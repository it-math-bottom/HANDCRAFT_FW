package com.example.handcraft.framework.web;

import com.example.handcraft.framework.web.beans.BarBean;

public class SampleMainProgram {

	public static void main(String[] args) {
		
		MyContext.autoRegister();

		BarBean bar = (BarBean) MyContext.getBean(BarBean.class.getName());
		bar.showMyName();
		
	}
}
