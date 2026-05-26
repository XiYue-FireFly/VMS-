package org.jeecg.modules.wms.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.wms.goods.entity.WmsProductImages;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.mapper.WmsProductsMapper;
import org.jeecg.modules.wms.goods.service.IWmsCargoOwnersService;
import org.jeecg.modules.wms.goods.service.IWmsProductCategoriesService;
import org.jeecg.modules.wms.goods.service.IWmsProductImagesService;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description: 商品信息表
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Service
public class WmsProductsServiceImpl extends ServiceImpl<WmsProductsMapper, WmsProducts> implements IWmsProductsService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IWmsCargoOwnersService wmsCargoOwnersService;

    @Autowired
    private IWmsProductCategoriesService wmsProductCategoriesService;

    @Autowired
    private IWmsProductImagesService wmsProductImagesService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(WmsProducts wmsProducts) {
        //校验货主加商品编码在商品表的唯一性
        if(checkProductCode(wmsProducts)){
            throw new RuntimeException("货主加商品编码在商品表中已存在");
        }
        //生成商品条码
        wmsProducts.setProductBarcode(generateProductBarcode(wmsProducts));
        //保存商品
        save(wmsProducts);
//        String productImgs= wmsProducts.getProductImgs();
//        if(productImgs!=null){
//            //图片
//            String[] productImgArr = productImgs.split(",");
//            for (String productImg : productImgArr) {
//                WmsProductImages wmsProductImages = new WmsProductImages();
//                wmsProductImages.setProductId(wmsProducts.getId());
//                wmsProductImages.setOriginal(productImg);
//                wmsProductImagesService.save(wmsProductImages);
//            }
//        }
    }

    /**
     * 校验货主加商品编码在商品表的唯一性
     * @param wmsProducts
     * @return 不存在返回false，存在返回true
     */
    public boolean checkProductCode(WmsProducts wmsProducts) {
        String ownerId = wmsProducts.getOwnerId();
        String productCode = wmsProducts.getProductCode();
        //根据货主和商品编码查询
        List<WmsProducts> wmsProductsList = baseMapper.selectList(lambdaQuery().getWrapper()
                .eq(WmsProducts::getOwnerId,ownerId)
                .eq(WmsProducts::getProductCode,productCode));
        return wmsProductsList.size()>0;

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(WmsProducts wmsProducts) {
        String id = wmsProducts.getId();
        WmsProducts wmsProductsOld = getById(id);
        //如果商品编码有变化
        if(!wmsProductsOld.getProductCode().equals(wmsProducts.getProductCode())){
            //校验商品编码在商品表中的 uniqueness
            if(!checkProductCode(wmsProducts)){
                throw new RuntimeException("商品编码在商品表中已存在");
            }
        }
        //如果商品条码为空则生成商品条码
        if(wmsProducts.getProductBarcode().isEmpty()){
            wmsProducts.setProductBarcode(generateProductBarcode(wmsProducts));
        }
        updateById(wmsProducts);
        //如果商品图片有变化
//        String productImgsOld = wmsProductsOld.getProductImgs()==null?"":wmsProductsOld.getProductImgs();
//        String productImgsNew = wmsProducts.getProductImgs()==null?"":wmsProducts.getProductImgs();
//        if(!productImgsOld.equals(productImgsNew)){
//            LambdaQueryWrapper<WmsProductImages> queryWrapper = new LambdaQueryWrapper<WmsProductImages>().eq(WmsProductImages::getProductId, id);
//            wmsProductImagesService.remove(queryWrapper);
//            if(productImgsNew!=null){
//                //图片
//                String[] productImgArr = productImgsNew.split(",");
//                for (String productImg : productImgArr) {
//                    WmsProductImages wmsProductImages = new WmsProductImages();
//                    wmsProductImages.setProductId(wmsProducts.getId());
//                    wmsProductImages.setOriginal(productImg);
//                    wmsProductImagesService.save(wmsProductImages);
//                }
//            }
//        }
    }


    /**
     * 生成商品条码全局唯一
     * @return
     */
    public String generateProductBarcode(WmsProducts wmsProducts) {
        //编码规则：6位货主编码+4位商品类别+6位序号
        //货主编码
        String ownerCode = wmsCargoOwnersService.getById(wmsProducts.getOwnerId()).getOwnerCode();
        //商品类型编码
        String categoryCode = wmsProductCategoriesService.getById(wmsProducts.getCategoryId()).getCategoryCode();

        String code = null;
        try {
            code = String.format("%06d", redisUtil.incr("WMS_PRO_BARCODE", 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        code = ownerCode + categoryCode + code;
        return code;
    }


}
