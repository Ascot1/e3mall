package cn.e3mall.content.service;

import java.util.List;

import cn.e3mall.common.pojo.EasyUITreeNode;

/**
 * 内容分类接口
 * @author cldn1
 *
 */

public interface ContentCatService {
	//获取内容分类节点
	public List<EasyUITreeNode> getContentCat(long parentId);
	
}
