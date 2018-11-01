package cn.e3mall.content.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.e3mall.common.pojo.EasyUITreeNode;
import cn.e3mall.content.service.ContentCatService;
import cn.e3mall.mapper.TbContentCategoryMapper;
import cn.e3mall.pojo.TbContentCategory;
import cn.e3mall.pojo.TbContentCategoryExample;
import cn.e3mall.pojo.TbContentCategoryExample.Criteria;

@Service
public class ContentCatServiceImpl implements ContentCatService {

	@Autowired
	TbContentCategoryMapper contentcatMapper;
	
	@Override
	public List<EasyUITreeNode> getContentCat(long parentId) {
		//添加查询条件
		TbContentCategoryExample example = new TbContentCategoryExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
		List<TbContentCategory> categoryList = contentcatMapper.selectByExample(example);
		//转换为EasyUITreeNode
		List<EasyUITreeNode> nodeList = new ArrayList<EasyUITreeNode>();
		for(TbContentCategory tc : categoryList) {
			EasyUITreeNode node = new EasyUITreeNode();
			node.setId(tc.getId());
			node.setState(tc.getIsParent()?"closed":"open");
			node.setText(tc.getName());
			nodeList.add(node);
		}
		//将查询结果返回
		return nodeList;
	}

}
