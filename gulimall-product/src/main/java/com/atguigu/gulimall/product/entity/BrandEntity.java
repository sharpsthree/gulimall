package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGruop;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStausGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 * 
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-01 23:12:37
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改时必须携带品牌id", groups = {UpdateGroup.class, UpdateStausGroup.class})
	@Null(message = "添加时品牌id必须为空", groups = {AddGruop.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名必须提交", groups = {UpdateGroup.class,AddGruop.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(message = "新增时品牌图片不能为空",groups = {AddGruop.class})
	@URL(message = "必须是合法的url地址",groups = {UpdateGroup.class,AddGruop.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@ListValue(vals= {0,1},message = "必须是1 or 0",groups = {UpdateGroup.class,AddGruop.class, UpdateStausGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty
	@Pattern(regexp = "^[A-Z]$", message = "检索首字母必须是一个A-Z的字母",groups = {UpdateGroup.class,AddGruop.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "排序不能为空",groups = {UpdateGroup.class,AddGruop.class})
	@Min(value = 0,message = "排序必须大于等于0", groups = {UpdateGroup.class,AddGruop.class})
	private Integer sort;

}
