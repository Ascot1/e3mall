package cn.e3mall.content.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.IDUtils;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.content.service.OperaterCatService;
import cn.e3mall.mapper.TbContentCategoryMapper;
import cn.e3mall.mapper.TbContentMapper;
import cn.e3mall.pojo.TbContent;
import cn.e3mall.pojo.TbContentCategory;
import cn.e3mall.pojo.TbContentExample;
import cn.e3mall.pojo.TbContentExample.Criteria;

@Service
public class OperaterCatServiceImpl implements OperaterCatService {

	@Autowired
	private TbContentCategoryMapper tcm; 
	
	@Autowired
	private TbContentMapper contentMapper;
	
	@Autowired
	private JedisClient jedisClient;
	
	@Value("${CONTENT_KEY}")
	private String CONTENT_KEY;
	
	/**
	 * parentId:父ID
	 * name:类别名
	 */
	@Override
	public E3Result addCatNode(long parentId, String name) {
        //创建一个TbContentCategory对象
		TbContentCategory category = new TbContentCategory();
		//补全TbContentCategory对象
		category.setName(name);
		category.setCreated(new Date());
		category.setUpdated(new Date());
		category.setParentId(parentId);
		//排列序号，表示同级类目的展示次序，如数值相等则按名称次序排序，取值范围：大于零的整数
		category.setSortOrder(1);
		//状态。可选值：1（正常），2（删除）
		category.setStatus(1);
	    category.setIsParent(false);
	    //将该节点的上一级节点改为父节点
	    TbContentCategory tc = tcm.selectByPrimaryKey(parentId);
	    if(!tc.getIsParent()) {
	    	tc.setIsParent(true);
	    	tcm.updateByPrimaryKey(tc);
	    }
	    //将该节点插入到数据库中
	    tcm.insert(category);
		//返回结果,这里的参数是根据jsp页面所需要返回的结果进行选择的：data.data.id，这个data就是指E3Result,data.data就是category，之后.id就是category.id
		return E3Result.ok(category);
	}

	/**
	 * 新增內容保存
	 */
	@Override
	public E3Result addContent(TbContent content) {
		//新增内容时，将缓存中的数据清除(缓存同步)
		jedisClient.hdel(CONTENT_KEY,content.getCategoryId().toString());		
		//补全TbCotnent
		content.setCreated(new Date());
	    content.setUpdated(new Date());
	  //执行数据库操作
		contentMapper.insert(content);
		return E3Result.ok();
	}

	/**
	 * 根据cid在content.jsp页面显示内容列表
	 */
	@Override
	public EasyUIDataGridResult findContentByCid(long categoryId,int page,int rows) {
		//设置分页信息
		PageHelper.startPage(page, rows);	
		//创建查询对象
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);
		//获取查询结果
	    List<TbContent> list = contentMapper.selectByExample(example);
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setRows(list);
	    //取分页结果
	    PageInfo<TbContent> pageInfo = new PageInfo<>(list);
		//取总记录数
	    long total = pageInfo.getTotal();
        result.setTotal(total);		
	    return result;
	}

	/**
	 * 根据所选id编辑内容
	 */
	@Override
	public E3Result editContent(TbContent content) {
		//根据id进行更新数据
		TbContentExample example = new TbContentExample();
		content.setUpdated(new Date());
		contentMapper.updateByPrimaryKey(content);
		return E3Result.ok();
	}
	
	/**
	 * 根据内容分类id查询内容列表
	 */
	@Override
	public List<TbContent> getContentListByCid(long cid) {
      //查询缓存，如果缓存中存在该内容，则不执行下面数据的操作
		try {
			String json = jedisClient.hget(CONTENT_KEY,cid+"");
			//判断json是否为空
			if(StringUtils.isNotBlank(json)) {
              //把json转换为list
				List<TbContent> list = JsonUtils.jsonToList(json,TbContent.class);
				return list;
			}
			
		} catch (Exception e) {
            e.printStackTrace();
		}
		
		TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();
        //设置查询条件
        criteria.andCategoryIdEqualTo(cid);
        //执行查询
        List<TbContent> list = contentMapper.selectByExample(example);
        //将查询结果加入到缓存中
        try {
        	jedisClient.hset(CONTENT_KEY, cid +"",JsonUtils.objectToJson(list));
        }catch(Exception e) {
        	e.printStackTrace();
        }
		return list;
	}

	//根据内容ID获取内容
	@Override
	public TbContent getContent(Long contentId) {
        //执行查询
		TbContent content = contentMapper.selectByPrimaryKey(contentId);
		//将查询结果返回
		return content;
	}

	//根据所选id删除内容信息
	@Override
	public E3Result delContent(long[] ids) {
		//根据id从数据库中删除该内容信息
		for(long id : ids) {
			contentMapper.deleteByPrimaryKey(id);
		}
		return E3Result.ok();
	}

	
}
