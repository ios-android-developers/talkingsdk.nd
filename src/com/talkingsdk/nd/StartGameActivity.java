package com.talkingsdk.nd;

import org.cocos2dx.lib.Cocos2dxActivity;

import android.os.Bundle;

/**
 * 欢迎界面
 *
 */
public class StartGameActivity extends  Cocos2dxActivity{
	static StartGameActivity _instance = null;
	public static StartGameActivity getInstance()
	{
		return _instance;
	}
	protected void onCreate(Bundle savedInstanceState){
		_instance =  this;
		super.onCreate(savedInstanceState);
	}

    static {
    	System.out.print("loadLibrary");
        System.loadLibrary("game");
    }
}
