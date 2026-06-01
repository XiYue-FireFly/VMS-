package org.jeecg.modules.wms.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2025/9/22 18:54
 */
@Component
public class WebSocketPush {
    @Autowired
    private WebSocketTest webSocketTest;

    /**
     * 定时推送数据
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void testpush() {
        //生成10以内的随机数
        int random = (int) (Math.random() * 10);
        webSocketTest.pushMessage("你好"+random);
    }
}
