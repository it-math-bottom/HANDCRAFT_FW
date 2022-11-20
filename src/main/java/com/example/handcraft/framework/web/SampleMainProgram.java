package com.example.handcraft.framework.web;

import com.example.handcraft.framework.web.beans.BarBean;
import com.example.handcraft.framework.web.beans.FooBean;

public class SampleMainProgram {

	public static void main(String[] args) {
		
		MyContext.registerType(FooBean.class.getName(), FooBean.class);
		MyContext.registerType(BarBean.class.getName(), BarBean.class);
		
		BarBean bar = (BarBean) MyContext.getBean(BarBean.class.getName());
		bar.showMyName();
		
	}
}
