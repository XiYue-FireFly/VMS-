package org.jeecg.modules.wms.ai.chat.controller;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.service.IAiragAppService;
import org.jeecg.modules.airag.app.vo.ChatSendParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Mr.M
 * @version 1.0
 * @description 星辰wms的AI接口
 * @date 2025/12/6 10:46
 */
@RestController
@Slf4j
@RequestMapping("/ai")
public class ChatController {

    //    @Resource(name = "chatClientOllama")
    @Resource(name = "chatClientOpenAi")
    private ChatClient chatClient;

    @Autowired
    private IAiragAppService airagAppService;

    // 请求方式和路径不要改动，将来要与前端联调
//    @RequestMapping("/chat")
//    public String chat(String prompt) {
//        //调用大模型
//        String content = chatClient
//                .prompt(prompt)
//                .call()//同步调用方法，等大模型全部返回才结束
//                .content();
//        return content;
//    }

    @RequestMapping(value = "/chat", produces = "text/html;charset=UTF-8")//加produces为了防止乱码
    public Flux<String> chat(String prompt) {
        //调用大模型
        Flux<String> flux = chatClient
                .prompt(prompt)
                .stream()//流式调用
                .content();

        return flux;
    }

    @RequestMapping(value = "/chat/send")
    public SseEmitter send(@RequestBody ChatSendParams chatSendParams, HttpServletRequest httpRequest) {

        AssertUtils.assertNotEmpty("参数异常", chatSendParams);
        //用户提示词
        String userMessage = chatSendParams.getContent();
        AssertUtils.assertNotEmpty("至少发送一条消息", userMessage);

        // 获取会话信息
        String conversationId = chatSendParams.getConversationId();
        String topicId = oConvertUtils.getString(chatSendParams.getTopicId(), UUIDGenerator.generate());
        // 每次会话都生成一个新的,用来缓存emitter
        String requestId = UUIDGenerator.generate();
        SseEmitter emitter = new SseEmitter(-0L);
        // 获取app信息
        AiragApp app = null;
        if (oConvertUtils.isNotEmpty(chatSendParams.getAppId())) {
            app = airagAppService.getById(chatSendParams.getAppId());
        }
        Flux<String> flux = null;
        if(app == null){
           flux = chatClient
                    .prompt(userMessage)
                    .stream()//流式调用
                    .content();
        }else{
           flux = chatClient
                    .prompt(userMessage)
                    .user(userMessage)
                    .system(app.getPrompt())//添加系统提示词
                    .stream()//流式调用
                    .content();
        }

        /**
         * 是否正在思考
         */
        AtomicBoolean isThinking = new AtomicBoolean(false);

        //向前端发送两条消息
        try {
            sendMessage(emitter, conversationId, topicId, requestId, ">", "MESSAGE");
            sendMessage(emitter, conversationId, topicId, requestId, "\n> ", "MESSAGE");
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        flux.subscribe(
                data -> {
                    // 兼容推理模型
                    if ("<think>".equals(data)) {
                        isThinking.set(true);
                        data = "> ";
                    }
                    if ("</think>".equals(data)) {
                        isThinking.set(false);
                        data = "\n\n";
                    }
                    if (isThinking.get()) {
                        if (null != data && data.contains("\n")) {
                            data = "\n> ";
                        }
                    }
                    // 发送消息
                    try {
                        sendMessage(emitter, conversationId, topicId, requestId, data, "MESSAGE");
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    // 错误处理
                    emitter.completeWithError(error);
                },
                () -> {
                    // 完成处理
                    try {
                        sendEndMessage(emitter, conversationId, topicId, requestId);
                        sendEndMessage(emitter, conversationId, topicId, requestId);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                }
        );
        return emitter;
    }

    // 辅助方法：向客户端发送消息
    private void sendMessage(SseEmitter emitter, String conversationId, String topicId,
                             String requestId, String message,String event) throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("conversationId", conversationId);
        response.put("topicId", topicId);
        response.put("requestId", requestId);
        response.put("event", event);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", message);
        response.put("data", messageData);

        String jsonData =  JSONObject.toJSONString(response);
        emitter.send(SseEmitter.event().data(jsonData));
    }
    // 辅助方法：发送结束消息
    private void sendEndMessage(SseEmitter emitter, String conversationId, String topicId,
                                String requestId) throws IOException {
        Map<String, Object> endResponse = new HashMap<>();
        endResponse.put("event", "MESSAGE_END");
        endResponse.put("flowId", null);
        endResponse.put("requestId", requestId);
        endResponse.put("conversationId", conversationId);
        endResponse.put("topicId", topicId);
        endResponse.put("data", null);

        String endJson =  JSONObject.toJSONString(endResponse);
        emitter.send(SseEmitter.event().data(endJson));
    }
}
