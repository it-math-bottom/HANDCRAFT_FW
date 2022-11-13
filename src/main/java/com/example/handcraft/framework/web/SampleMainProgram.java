package com.example.handcraft.framework.web;

import com.example.handcraft.framework.web.beans.BarBean;
import com.example.handcraft.framework.web.beans.FooBean;

public class SampleMainProgram {

	public static void main(String[] args) {
		
		MyContext.registerType("fooBean", FooBean.class);
		MyContext.registerType("barBean", BarBean.class);
		
		BarBean bar = (BarBean) MyContext.getBean("barBean");
		bar.showMyName();
		
	}
}
