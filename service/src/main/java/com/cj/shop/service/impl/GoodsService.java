package com.cj.shop.service.impl;

import com.cj.shop.api.entity.GoodsBrand;
import com.cj.shop.api.entity.GoodsSupply;
import com.cj.shop.api.interf.GoodsApi;
import com.cj.shop.api.param.GoodsBrandRequest;
import com.cj.shop.api.param.GoodsSupplyRequest;
import com.cj.shop.api.response.PagedList;
import com.cj.shop.dao.mapper.GoodsBrandMapper;
import com.cj.shop.dao.mapper.GoodsSupplyMapper;
import com.cj.shop.service.cfg.JedisCache;
import com.cj.shop.service.consts.ResultMsg;
import com.cj.shop.service.util.PropertiesUtil;
import com.cj.shop.service.util.ResultMsgUtil;
import com.cj.shop.service.util.ValidatorUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品service
 *
 * @author yuchuanWeng(wycmiko @ foxmail.com)
 * @date 2018/6/25
 * @since 1.0
 */
@Transactional(rollbackFor = Exception.class)
@Slf4j
@Service
public class GoodsService implements GoodsApi {
    @Autowired
    private JedisCache jedisCache;
    @Autowired
    private GoodsSupplyMapper goodsSupplyMapper;
    @Autowired
    private GoodsBrandMapper goodsBrandMapper;

    private static final String JEDIS_PREFIX_GOODS_TYPE = "cj_shop:mall:goods:";
    private static final String JEDIS_PREFIX_GOODS_SUPPLY = JEDIS_PREFIX_GOODS_TYPE + "supply:";
    private static final String JEDIS_PREFIX_GOODS_BRAND = JEDIS_PREFIX_GOODS_TYPE + "brand:";

    /**
     * 添加供应商
     *
     * @param request
     * @return
     */
    @Override
    public String insertSupply(GoodsSupplyRequest request) {
        GoodsSupply supply = new GoodsSupply();
        BeanUtils.copyProperties(request, supply);
        supply.setProperties(PropertiesUtil.addProperties(request.getProperties()));
        int i = goodsSupplyMapper.insertSelective(supply);
        if (i > 0) {
            jedisCache.hset(JEDIS_PREFIX_GOODS_SUPPLY, supply.getId().toString(), goodsBrandMapper.selectByPrimaryKey(supply.getId()));
        }
        return ResultMsgUtil.dmlResult(i);
    }

    /**
     * 修改供应商(包含禁用)
     *
     * @param request
     * @return
     */
    @Override
    public String updateSupply(GoodsSupplyRequest request) {
        GoodsSupply supplyDetail = goodsSupplyMapper.selectByPrimaryKey(request.getId());
        if (supplyDetail == null) {
            return ResultMsg.SUPPLY_NOT_EXISTS;
        }
        supplyDetail.setProperties(PropertiesUtil.changeProperties(supplyDetail.getProperties(), request.getProperties()));
        BeanUtils.copyProperties(request, supplyDetail);
        int i = goodsSupplyMapper.updateByPrimaryKeySelective(supplyDetail);
        if (i > 0) {
            jedisCache.hset(JEDIS_PREFIX_GOODS_SUPPLY, request.getId().toString(), goodsSupplyMapper.selectByPrimaryKey(request.getId()));
        }
        return ResultMsgUtil.dmlResult(i);
    }

    /**
     * 查询全部供应商
     *
     * @param supplyName 供应商名称模糊匹配
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PagedList<GoodsSupply> findAllSupplies(String supplyName, Integer pageNum, Integer pageSize, String type) {
        Integer flag = null;
        if (!"all".equals(type.toLowerCase())) {
            flag = 1;
        }
        Page<Object> objects = null;
        List<GoodsSupply> list = null;
        if (pageNum != null && pageSize != null) {
            objects = PageHelper.startPage(pageNum, pageSize);
        } else {
            pageNum = 0;
            pageSize = 0;
        }
        List<Long> longs = ValidatorUtil.checkNotEmpty(goodsSupplyMapper.selectAllSupplyIds(supplyName, flag));
        if (!longs.isEmpty()) {
            list = new ArrayList<>();
            for (Long id : longs) {
                GoodsSupply supplyDetail = getSupplyDetail(id);
                list.add(supplyDetail);
            }
        }
        PagedList<GoodsSupply> pagedList = new PagedList<>(list, objects == null ? 0 : objects.getTotal(), pageNum, pageSize);
        return pagedList;
    }

    /**
     * 查询供应商明细
     *
     * @param supplyId
     * @return
     */
    @Override
    public GoodsSupply getSupplyDetail(Long supplyId) {
        GoodsSupply hget = jedisCache.hget(JEDIS_PREFIX_GOODS_SUPPLY, supplyId.toString(), GoodsSupply.class);
        if (hget == null) {
            hget = goodsSupplyMapper.selectByPrimaryKey(supplyId);
            jedisCache.hset(JEDIS_PREFIX_GOODS_SUPPLY, supplyId.toString(), hget);
        }
        return hget;
    }

    /**
     * 添加品牌
     *
     * @param request
     * @return
     */
    @Override
    public String insertBrand(GoodsBrandRequest request) {
        GoodsBrand brand = new GoodsBrand();
        BeanUtils.copyProperties(request, brand);
        brand.setProperties(PropertiesUtil.addProperties(request.getProperties()));
        int i = goodsBrandMapper.insertSelective(brand);
        if (i > 0) {
            jedisCache.hset(JEDIS_PREFIX_GOODS_BRAND, brand.getId().toString(), goodsBrandMapper.selectByPrimaryKey(brand.getId()));
        }
        return ResultMsgUtil.dmlResult(i);
    }

    /**
     * 修改品牌
     *
     * @param request
     * @return
     */
    @Override
    public String updateBrand(GoodsBrandRequest request) {
        GoodsBrand brandDetail = getBrandDetail(request.getId());
        if (brandDetail == null) {
            return ResultMsg.BRAND_NOT_EXISTS;
        }
        GoodsBrand brand = new GoodsBrand();
        BeanUtils.copyProperties(request, brand);
        brand.setProperties(PropertiesUtil.changeProperties(brandDetail.getProperties(), request.getProperties()));
        int i = goodsBrandMapper.updateByPrimaryKeySelective(brand);
        if (i > 0) {
            jedisCache.hset(JEDIS_PREFIX_GOODS_BRAND, request.getId().toString(), goodsBrandMapper.selectByPrimaryKey(request.getId()));
        }
        return ResultMsgUtil.dmlResult(i);
    }

    /**
     * 查询品牌详情
     *
     * @param brandId
     * @return
     */
    @Override
    public GoodsBrand getBrandDetail(Long brandId) {
        GoodsBrand hget = jedisCache.hget(JEDIS_PREFIX_GOODS_BRAND, brandId.toString(), GoodsBrand.class);
        if (hget == null) {
            hget = goodsBrandMapper.selectByPrimaryKey(brandId);
            jedisCache.hset(JEDIS_PREFIX_GOODS_BRAND, brandId.toString(), hget);
        }
        return hget;
    }

    /**
     * 查询全部品牌
     *
     * @param brandName 品牌名模糊匹配
     * @param pageNum   当前页
     * @param pageSize  每页条数
     * @param type      类型 all=显示全部 其它显示未被禁用的
     * @return
     */
    @Override
    public PagedList<GoodsBrand> findAllBrands(String brandName, Integer pageNum, Integer pageSize, String type) {
        Integer flag = null;
        if (!"all".equals(type.toLowerCase())) {
            flag = 1;
        }
        Page<Object> objects = null;
        List<GoodsBrand> list = null;
        if (pageNum != null && pageSize != null) {
            objects = PageHelper.startPage(pageNum, pageSize);
        } else {
            pageNum = 0;
            pageSize = 0;
        }
        List<Long> longs = ValidatorUtil.checkNotEmpty(goodsBrandMapper.selectBrandIds(brandName, flag));
        if (!longs.isEmpty()) {
            list = new ArrayList<>();
            for (Long id : longs) {
                GoodsBrand brandDetail = getBrandDetail(id);
                list.add(brandDetail);
            }
        }
        PagedList<GoodsBrand> pagedList = new PagedList<>(list, objects == null ? 0 : objects.getTotal(), pageNum, pageSize);
        return pagedList;
    }
}
