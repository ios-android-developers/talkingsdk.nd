package com.talkingsdk.nd;

import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.nd.commplatform.NdCommplatform;
import com.nd.commplatform.NdErrorCode;
import com.nd.commplatform.NdMiscCallbackListener;
import com.nd.commplatform.OnInitCompleteListener;
import com.nd.commplatform.NdMiscCallbackListener.OnLoginProcessListener;
import com.nd.commplatform.entry.NdAppInfo;
import com.nd.commplatform.entry.NdBuyInfo;
import com.nd.commplatform.entry.NdLoginStatus;
import com.talkingsdk.SdkBase;
import com.talkingsdk.models.LoginData;


public class SdkObject implements SdkBase{
	public static final int appID_91Bean = 100010;//91appID
	public static final String appKEY_91Bean = "C28454605B9312157C2F76F27A9BCA2349434E546A6E9C75";//91appKEY
	private Activity _startActivity;
	private LoginData _loginData;

	public void startActivity( Intent intent )
	{
		this._startActivity.startActivity(intent);
	}
	Activity getParentActivity()
	{
		return this._startActivity;
	}
	public void initSdk(Object obj){
		
	}
	
	public void initGame(Object obj){
		System.out.println("NdSdk init");
	}
	
	public void exit() {
		System.out.println("NdSdk exit");
	}
	
	private void doGameLogin(int code) {

		String tip = "";
		if (code == NdErrorCode.ND_COM_PLATFORM_SUCCESS) {
			if (NdCommplatform.getInstance().ndGetLoginStatus() == NdLoginStatus.AccountLogin) {// 账号登录
				_loginData = new LoginData();
				_loginData.setUsername(NdCommplatform.getInstance().getLoginUin());
				_loginData.setSessionId(NdCommplatform.getInstance().getSessionId());
				// 账号登录成功，此时可用初始化玩家游戏数据
				tip = "账号登录成功";
				getParentActivity().finish();
				if( StartGameActivity.getInstance() == null ){
					startActivity(new Intent(getParentActivity(), StartGameActivity.class));
				}
			} else if (NdCommplatform.getInstance().ndGetLoginStatus() == NdLoginStatus.GuestLogin) {// 游客登录
				// 游客登录成功，此时可获取玩家的游客UIN做为保存游戏数据的标识，玩家游客账号转正后该UIN不变。
				tip = "游客登录成功";
			}
		} else if (code == NdErrorCode.ND_COM_PLATFORM_ERROR_CANCEL) {
			tip = "取消账号登录";
		} else if (code == NdErrorCode.ND_COM_GUEST_OFFICIAL_SUCCESS) {
			tip = "游客转正成功";
		} else {
			tip = "登录失败，错误代码：" + code;
		}
	}
	@Override
	public void initUI(Activity parentActivity) {
		// TODO Auto-generated method stub
		this._startActivity = parentActivity;
		OnInitCompleteListener mOnInitCompleteListener = new OnInitCompleteListener() {

			@Override
			protected void onComplete(int ndFlag) {
				switch (ndFlag) {
				case OnInitCompleteListener.FLAG_NORMAL:
					NdCommplatform.getInstance().ndSetScreenOrientation(NdCommplatform.SCREEN_ORIENTATION_AUTO);
					// 初始化自己的游戏
					// 根据91自测文档我们需要首先打开登录界面
					login();
					break;
				case OnInitCompleteListener.FLAG_FORCE_CLOSE:
				default:
					 // 如果还有别的Activity或资源要关闭的在这里处理
					break;
				}
			}
		};

	    NdAppInfo appInfo = new NdAppInfo();
	    appInfo.setCtx(this._startActivity);
	    appInfo.setAppId(appID_91Bean);// 应用ID
	    appInfo.setAppKey(appKEY_91Bean);// 应用Key
	    /*  
	     * NdVersionCheckLevelNormal 版本检查失败可以继续进行游戏 NdVersionCheckLevelStrict
	     * 版本检查失败则不能进入游戏 默认取值为NdVersionCheckLevelStrict
	     */
	    appInfo.setNdVersionCheckStatus(NdAppInfo.ND_VERSION_CHECK_LEVEL_STRICT);

	    // 初始化91SDK
	    NdCommplatform.getInstance().ndInit(this._startActivity, appInfo, mOnInitCompleteListener);
	}

	@Override
	public void login() {
		NdCommplatform.getInstance().ndLogin(getParentActivity(),
			new NdMiscCallbackListener.OnLoginProcessListener() {
				@Override
				public void finishLoginProcess(int code) {
					doGameLogin(code);
				}
			}
		);

	}
    public void changeAccount()
    {
		NdCommplatform.getInstance().ndEnterAccountManage(StartGameActivity.getInstance(), new OnLoginProcessListener(){

			@Override
			public void finishLoginProcess(int code) {
				// TODO Auto-generated method stub
				// 登录的返回码检查
				String tip = null;
				if (code == NdErrorCode.ND_COM_PLATFORM_SUCCESS) { 
					doGameLogin(code);
				} else if (code == NdErrorCode.ND_COM_PLATFORM_ERROR_CANCEL) {
//					tip = "取消账号切换"; 
				} else  {
					getParentActivity().finish(); 
					Intent intent = new Intent(StartGameActivity.getInstance(),getParentActivity().getClass());	
					StartGameActivity.getInstance().startActivity(intent);
//					tip = "账号切换失败"; 
				}
			}
			
		});
	}
	@Override
	public void logout() {
		NdCommplatform.getInstance().ndLogout(NdCommplatform.LOGOUT_TO_NON_RESET_AUTO_LOGIN_CONFIG, this._startActivity);
	}

	@Override
	public LoginData getLoginData() {
		return _loginData;
	}

	@Override
	public void pay() {
		StartGameActivity.getInstance().runOnUiThread(new Runnable() {
			public void run() {
				NdBuyInfo buyInfo = new NdBuyInfo();
				String serial = UUID.randomUUID().toString();
				buyInfo.setSerial(serial.toString());// 订单号唯一(不能为空)
				buyInfo.setProductId("680254");// 商品ID,厂商也可以使用固定商品ID 例如“1”
				buyInfo.setProductName("苹果");// 产品名称
				buyInfo.setProductPrice(0.01);// 产品现价 (不能小于0.01个91豆)
				buyInfo.setProductOrginalPrice(2.60);// 产品原价,同上面的价格
				buyInfo.setCount(3);// 购买数量(商品数量最大10000,最新1)
				buyInfo.setPayDescription("gamezoon1");// 服务器分区,不超过20个字符,只允许英文或数字
				int aError = NdCommplatform.getInstance().ndUniPayAsyn(buyInfo,
						StartGameActivity.getInstance(),
						new NdMiscCallbackListener.OnPayProcessListener() {
							@Override
							public void finishPayProcess(int code) {
								switch (code) {
								case NdErrorCode.ND_COM_PLATFORM_SUCCESS:
									Toast.makeText(getParentActivity(), "购买成功",
											Toast.LENGTH_SHORT).show();
									break;
								case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_FAILURE:
									Toast.makeText(getParentActivity(), "购买失败",
											Toast.LENGTH_SHORT).show();
									break;
								case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_CANCEL:
									Toast.makeText(getParentActivity(), "取消购买",
											Toast.LENGTH_SHORT).show();
									break;
								case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_ASYN_SMS_SENT:
									Toast.makeText(getParentActivity(),
											"订单已提交,充值短信已发送", Toast.LENGTH_SHORT)
											.show();
									break;
								case NdErrorCode.ND_COM_PLATFORM_ERROR_PAY_REQUEST_SUBMITTED:
									System.err.printf("%s", "订单已提交");
									Toast.makeText(getParentActivity(),
											"订单已提交", Toast.LENGTH_SHORT).show();
									break;
								default:
									Toast.makeText(getParentActivity(), "购买失败",
											Toast.LENGTH_SHORT).show();
								}
							}
						});
				if (aError != 0) {
					Toast.makeText(getParentActivity(), "您输入参数有错,无法提交购买请求",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

	}
}
