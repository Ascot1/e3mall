package cn.e3mall.portal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.e3mall.content.service.OperaterCatService;
import cn.e3mall.pojo.TbContent;

@Controller
public class IndexController {
    @Autowired
    OperaterCatService contentService;
    
    @Value("${CONTENT_CAROUSEL_ID}")
    private Long CONTENT_CAROUSEL_ID;
    
    @Value("${CONTENT_WIIBUY_ID}")
    private Long CONTENT_WIIBUY_ID;
    
	@RequestMapping("/index")
	public String showIndex(Model model) {
		List<TbContent> listByCid = contentService.getContentListByCid(CONTENT_CAROUSEL_ID);
		List<TbContent> willList = contentService.getContentListByCid(CONTENT_WIIBUY_ID);
		//把结果传递给页面
		model.addAttribute("ad1List",listByCid);
		model.addAttribute("willList", willList);
		return "index";
	}
}
