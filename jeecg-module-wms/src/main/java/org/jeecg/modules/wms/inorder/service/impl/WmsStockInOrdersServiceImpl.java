package org.jeecg.modules.wms.inorder.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrders;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrderItems;
import org.jeecg.modules.wms.inorder.mapper.WmsStockInOrderItemsMapper;
import org.jeecg.modules.wms.inorder.mapper.WmsStockInOrdersMapper;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrderItemsService;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrdersService;
import org.jeecg.modules.wms.inorder.vo.WmsStockInOrdersPage;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 入库单主表
 * @Author: jeecg-boot
 * @Date:   2025-11-23
 * @Version: V1.0
 */
@Service
public class WmsStockInOrdersServiceImpl extends ServiceImpl<WmsStockInOrdersMapper, WmsStockInOrders> implements IWmsStockInOrdersService {

	@Autowired
	private WmsStockInOrdersMapper wmsStockInOrdersMapper;
	@Autowired
	private WmsStockInOrderItemsMapper wmsStockInOrderItemsMapper;

	@Autowired
	protected IWmsStockInOrderItemsService wmsStockInOrderItemsService;

	@Autowired
	private RedisUtil redisUtil;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveMain(WmsStockInOrders wmsStockInOrders, List<WmsStockInOrderItems> wmsStockInOrderItemsList) {
		wmsStockInOrdersMapper.insert(wmsStockInOrders);
		if(wmsStockInOrderItemsList!=null && wmsStockInOrderItemsList.size()>0) {
			for(WmsStockInOrderItems entity:wmsStockInOrderItemsList) {
				//外键设置
				entity.setOrderId(wmsStockInOrders.getId());
				wmsStockInOrderItemsMapper.insert(entity);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateMain(WmsStockInOrders wmsStockInOrders,List<WmsStockInOrderItems> wmsStockInOrderItemsList) {

		//校验wmsStockInOrderItemsList不能为空
		if(wmsStockInOrderItemsList==null || wmsStockInOrderItemsList.size()<=0) {
			throw new JeecgBootException("入库单明细不能为空");
		}

		//先查询入库单
		WmsStockInOrders old = wmsStockInOrdersMapper.selectById(wmsStockInOrders.getId());
		if(old==null) {
			throw new JeecgBootException("入库单不存在");
		}
		//入库单状态
		String status = old.getStatus();

		//初始状态、审核失败状态可以修改
		if(!(WarehouseDictEnum.INBOUND_INITIAL.getCode().equals(status) || WarehouseDictEnum.INBOUND_REJECTED.getCode().equals(status))) {
			//抛出异常
			throw new JeecgBootException("初始状态、审核失败状态方可修改");
		}

		//修改入库单的基本信息
		wmsStockInOrdersMapper.updateById(wmsStockInOrders);

		//1.先删除子表数据
		wmsStockInOrderItemsMapper.deleteByMainId(wmsStockInOrders.getId());

		//对wmsStockInOrderItemsList的数据按商品分级 <key:商品id，value：明细列表>
		Map<String, List<WmsStockInOrderItems>> collect = wmsStockInOrderItemsList.stream().collect(Collectors.groupingBy(WmsStockInOrderItems::getProductId));

		//定义一 个合并后的list
		List<WmsStockInOrderItems> mergeList = new ArrayList<>();

		//遍历map中的key/value对
		for (Map.Entry<String, List<WmsStockInOrderItems>> entry : collect.entrySet()) {
			//取出key/value中的value即明细列表
			List<WmsStockInOrderItems> list = entry.getValue();
			WmsStockInOrderItems wmsStockInOrderItems = list.get(0);
			//如果list的size大于1才进行求和
			if(list.size()>1) {
				//对list中的采购数量进行求和
				Integer totalExpectedQuantity = list.stream().mapToInt(WmsStockInOrderItems::getExpectedQuantity).sum();
				wmsStockInOrderItems.setExpectedQuantity(totalExpectedQuantity);
			}
			//在明细中设置入库单的id
			wmsStockInOrderItems.setOrderId(wmsStockInOrders.getId());

			mergeList.add(wmsStockInOrderItems);

		}

		//将mergeList插入数据库
		boolean b = wmsStockInOrderItemsService.saveBatch(mergeList);
		if(!b) {
			throw new JeecgBootException("保存失败");
		}
	}
//	@Override
//	@Transactional(rollbackFor = Exception.class)
//	public void updateMain(WmsStockInOrders wmsStockInOrders,List<WmsStockInOrderItems> wmsStockInOrderItemsList) {
//		wmsStockInOrdersMapper.updateById(wmsStockInOrders);
//
//		//1.先删除子表数据
//		wmsStockInOrderItemsMapper.deleteByMainId(wmsStockInOrders.getId());
//
//		//2.子表数据重新插入
//		if(wmsStockInOrderItemsList!=null && wmsStockInOrderItemsList.size()>0) {
//			for(WmsStockInOrderItems entity:wmsStockInOrderItemsList) {
//				//外键设置
//				entity.setOrderId(wmsStockInOrders.getId());
//				wmsStockInOrderItemsMapper.insert(entity);
//			}
//		}
//	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String id) {
		wmsStockInOrderItemsMapper.deleteByMainId(id);
		wmsStockInOrdersMapper.deleteById(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> idList) {
		for(Serializable id:idList) {
			wmsStockInOrderItemsMapper.deleteByMainId(id.toString());
			wmsStockInOrdersMapper.deleteById(id);
		}
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void add(WmsStockInOrders wmsStockInOrders) {
		//自动生成入库单编号
		String orderNumber = generateOrderNumber();
		//设置初始状态为“初始”
		wmsStockInOrders.setStatus(WarehouseDictEnum.INBOUND_INITIAL.getCode());
		wmsStockInOrders.setOrderNumber(orderNumber);
		int insert = wmsStockInOrdersMapper.insert(wmsStockInOrders);
		if(insert<1) {
			throw new JeecgBootException("保存失败");
		}
	}

	/**
	 * 生成入库单编码
	 * 共12位，由年月日(yyyyMMdd)+4位序号
	 * 4位序号 使用redis 自增序列实现
	 * key: 8位年月日
	 * value: 4位序号
	 * @return
	 */
	public String generateOrderNumber() {

		//当前时间年月日(yyyyMMdd) now() yyyy-MM-dd HH:mm:ss
		String time = DateUtils.now().substring(0, 10).replaceAll("-", "");
		//key
		String key = "wms:asn_number"+time;
		long incr = 0;
		try {
			incr = redisUtil.incr(key, 1);

			if(incr==1){//当key创建的时候才设置过期
				//设置过期时间 24小时+60秒
				redisUtil.expire(key, 60 * 60 * 24+60);
			}

		} catch (Exception e) {
			throw new JeecgBootException("生成入库单编码异常");
		}
		//入库单号
		String orderNumber = "ASN"+time + String.format("%04d", incr);
		return orderNumber;

	}
	/**
	 * 审核入库单
	 * @param wmsStockInOrdersPage
	 */
	public void audit(WmsStockInOrdersPage wmsStockInOrdersPage){
		//如果没有选择入库单则返回错误
		if(oConvertUtils.isEmpty(wmsStockInOrdersPage.getId())){
			//抛出异常
			throw new JeecgBootException("请选择要审核的入库单");
		}
		WmsStockInOrders wmsStockInOrdersEntity = getById(wmsStockInOrdersPage.getId());
		if(wmsStockInOrdersEntity==null) {
			//异常
			throw new JeecgBootException("入库单不存在");
		}

		//当前状态只能是提交审核状态
		if(!WarehouseDictEnum.INBOUND_SUBMIT_AUDIT.getCode().equals(wmsStockInOrdersEntity.getStatus())){
			//异常
			throw new JeecgBootException("当前状态是提交审核状态方可进行审核");
		}

		//如果审核失败则更新状态
		if(WarehouseDictEnum.INBOUND_REJECTED.getCode().equals(wmsStockInOrdersPage.getStatus())){
			wmsStockInOrdersEntity.setStatus(wmsStockInOrdersPage.getStatus());
			updateById(wmsStockInOrdersEntity);
			return;
		}
		//入库单明细
		List<WmsStockInOrderItems> wmsStockInOrderItemsList = wmsStockInOrderItemsService.selectByMainId(wmsStockInOrdersPage.getId());
		if(wmsStockInOrderItemsList==null || wmsStockInOrderItemsList.size()<=0){
			//异常
			throw new JeecgBootException("请添加入库明细");
		}
		//更新状态为审核通过或审核不通过
		wmsStockInOrdersEntity.setStatus(wmsStockInOrdersPage.getStatus());
		updateById(wmsStockInOrdersEntity);
	}
	/**
	 * 提交审核
	 * @param wmsStockInOrdersPage
	 */
	public void submitAudit(WmsStockInOrdersPage wmsStockInOrdersPage){
		//如果没有选择入库单则返回错误
		if(wmsStockInOrdersPage==null || oConvertUtils.isEmpty(wmsStockInOrdersPage.getId())){
			//抛出异常
			throw new JeecgBootException("请选择要审核的入库单");
		}
		WmsStockInOrders wmsStockInOrdersEntity = getById(wmsStockInOrdersPage.getId());
		if(wmsStockInOrdersEntity==null) {
			//异常
			throw new JeecgBootException("入库单不存在");
		}
		//当前状态只能是初始状态、审核失败状态
		if(!(WarehouseDictEnum.INBOUND_INITIAL.getCode().equals(wmsStockInOrdersEntity.getStatus())
				|| WarehouseDictEnum.INBOUND_REJECTED.getCode().equals(wmsStockInOrdersEntity.getStatus()))){
			throw new JeecgBootException("非初始状态、审核失败状态入库单不允许审核");
		}
		//更新状态为提交审核
		wmsStockInOrdersEntity.setStatus(WarehouseDictEnum.INBOUND_SUBMIT_AUDIT.getCode());
		updateById(wmsStockInOrdersEntity);
	}

    @Override
    public String updateReceivedStatus(String stockInOrderId) {

		WmsStockInOrders stockInOrders = new WmsStockInOrders();
		stockInOrders.setId(stockInOrderId);
		//根据入库单id查询下边的明细
		List<WmsStockInOrderItems> wmsStockInOrderItems = wmsStockInOrderItemsService.selectByMainId(stockInOrderId);
		//统计明细中的收货总量(良品)及不良品总量
		//统计良品数量
		int goodQuantity = wmsStockInOrderItems.stream().mapToInt(WmsStockInOrderItems::getReceivedQuantity).sum();
		stockInOrders.setTotalReceivedQuantity(goodQuantity);
		//统计不良品数量
		int badQuantity = wmsStockInOrderItems.stream().mapToInt(WmsStockInOrderItems::getDefectiveQuantity).sum();
		stockInOrders.setTotalDefectiveQuantity(badQuantity);


		//只要有一个明细的状态不是收货完成，则入库单的状态不是收货完成
		boolean b = wmsStockInOrderItems.stream().anyMatch(wmsStockInOrderItems1 -> !WarehouseDictEnum.INBOUND_RECEIVED.getCode().equals(wmsStockInOrderItems1.getStatus()));

		if(b){//未收货完成
			stockInOrders.setStatus(WarehouseDictEnum.INBOUND_RECEIVING.getCode());

		}else{//收货完成
			stockInOrders.setStatus(WarehouseDictEnum.INBOUND_RECEIVED.getCode());
		}
		boolean b1 = updateById(stockInOrders);
		return stockInOrders.getStatus();
	}
}
