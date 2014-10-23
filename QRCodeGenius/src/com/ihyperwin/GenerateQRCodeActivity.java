package com.ihyperwin;

import com.google.zxing.WriterException;
import com.ihyperwin.R;
import com.ihyperwin.util.AlertManager;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboDownloadListener;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.exception.WeiboShareException;
import com.sina.weibo.AccessTokenKeeper;
import com.ihyperwin.util.Constants;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.platformtools.Util;
import com.zxing.encoding.EncodingHandler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class GenerateQRCodeActivity extends Activity implements
		IWeiboHandler.Response {

	/**用户输入的字符串*/
	private EditText qrStrEditText;

	/**生成的二维码图片*/
	private ImageView qrImgImageView;

	/** 微博微博分享接口实例 */
	private IWeiboShareAPI mWeiboShareAPI = null;

	/** 微博 Web 授权类，提供登陆等功能 */
	private WeiboAuth mWeiboAuth;

	/** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能 */
	private Oauth2AccessToken mAccessToken;

	/** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
	private SsoHandler mSsoHandler;
	
	/**微博分享按钮图片 */
	private ImageView weiboShareView;
	
	/**微信分享按钮图片 */
	private ImageView wechatShareView;
	
	/**微信API*/
	private IWXAPI wxApi;
	
	/**分享给朋友*/   
	private static final int ShareToFriend  =  0;
	
	/**分享到朋友圈*/
	private static final int ShareToFriendsCircle =  1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.generate_qrcode);

		qrStrEditText = (EditText) this.findViewById(R.id.et_qr_string);
		qrImgImageView = (ImageView) this.findViewById(R.id.iv_qr_image);
		weiboShareView = (ImageView) this.findViewById(R.id.weibo_share);
		wechatShareView = (ImageView) this.findViewById(R.id.wechat_share);

		Button generateQRCodeButton = (Button) this
				.findViewById(R.id.btn_add_qrcode);
		generateQRCodeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					String contentString = qrStrEditText.getText().toString();
					if (!contentString.equals("")) {
						Bitmap qrCodeBitmap = EncodingHandler.createQRCode(
								contentString, 350);
						
						qrImgImageView.setImageBitmap(qrCodeBitmap);
					} else {
						Toast.makeText(GenerateQRCodeActivity.this,
								"Text can not be empty", Toast.LENGTH_SHORT)
								.show();
					}

				} catch (WriterException e) {
					Log.e("generate QRCode Error",e.toString());
				}
			}
		});
		
		
		//微信实例化 start
		wxApi = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID);
		wxApi.registerApp(Constants.WX_APP_ID);
		//微信实例化 end

		// ********************************************************** 分享到微博功能
		// start
		// 创建微博分享接口实例
		// 创建微博实例
		mWeiboAuth = new WeiboAuth(this, Constants.APP_KEY,
				Constants.REDIRECT_URL, Constants.SCOPE);
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);

		// 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
		// 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
		// NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
		mWeiboShareAPI.registerApp();

		// 如果未安装微博客户端，设置下载微博对应的回调
		if (!mWeiboShareAPI.isWeiboAppInstalled()) {
			mWeiboShareAPI
					.registerWeiboDownloadListener(new IWeiboDownloadListener() {
						@Override
						public void onCancel() {
							Toast.makeText(GenerateQRCodeActivity.this,
									R.string.cancel_download_weibo,
									Toast.LENGTH_SHORT).show();
						}
					});
		}

		// 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
		// 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
		// 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
		// 失败返回 false，不调用上述回调
		if (savedInstanceState != null) {
			mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
		}

		weiboShareView.setImageDrawable( (BitmapDrawable) getResources().getDrawable(R.drawable.sina_logo));
		weiboShareView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			
				mAccessToken = AccessTokenKeeper
						.readAccessToken(GenerateQRCodeActivity.this);
				if (mAccessToken.isSessionValid()) {
					try {
						// 检查微博客户端环境是否正常，如果未安装微博，弹出对话框询问用户下载微博客户端
						if (mWeiboShareAPI.checkEnvironment(true)) {
							sendMessage();
						}
					} catch (WeiboShareException e) {
						Log.e("weiboShare", e.toString());
						Toast.makeText(GenerateQRCodeActivity.this,
								e.getMessage(), Toast.LENGTH_LONG).show();
					}
				} else {

					mSsoHandler = new SsoHandler(GenerateQRCodeActivity.this,
							mWeiboAuth);
					mSsoHandler.authorize(new AuthListener());

				}
			}
		});
		
		//填充微信图片
		wechatShareView.setImageDrawable( (BitmapDrawable) getResources().getDrawable(R.drawable.wechat_logo));
		wechatShareView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertManager.showAlert(GenerateQRCodeActivity.this, getString(R.string.send_img), 
						GenerateQRCodeActivity.this.getResources().getStringArray(R.array.send_img_item),
						null, new AlertManager.OnAlertSelectId(){

					@Override
					public void onClick(int whichButton) {						
						switch(whichButton){
						//分享到朋友圈
						case ShareToFriend: {
							sendPictureToWeChat(ShareToFriend);
							break;
						}
						//发送给朋友
						case ShareToFriendsCircle: {
							sendPictureToWeChat(ShareToFriendsCircle);
							break;
						}
						default:
							break;
						}
					}
					
				},null);
				
			}
			});
		
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
		// 来接收微博客户端返回的数据；执行成功，返回 true，并调用
		// {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
		mWeiboShareAPI.handleWeiboResponse(intent, this);
	}

	@Override
	public void onResponse(BaseResponse baseResp) {
		switch (baseResp.errCode) {
		case WBConstants.ErrorCode.ERR_OK:
			Toast.makeText(this, getString(R.string.share_success), Toast.LENGTH_LONG).show();
			break;
		case WBConstants.ErrorCode.ERR_CANCEL:
			Toast.makeText(this,  getString(R.string.share_cancel), Toast.LENGTH_LONG).show();
			break;
		case WBConstants.ErrorCode.ERR_FAIL:
			Toast.makeText(this,  getString(R.string.share_fail) + "Error Message: " + baseResp.errMsg,
					Toast.LENGTH_LONG).show();
			break;
		}

	}

	/**
	 * 第三方应用发送请求消息到微博，唤起微博分享界面。
	 */
	private void sendMessage() {

		WeiboMessage weiboMessage = new WeiboMessage();
		weiboMessage.mediaObject = getImageObj();
		// 2. 初始化从第三方到微博的消息请求
		SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
		// 用transaction唯一标识一个请求
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.message = weiboMessage;

		// 3. 发送请求消息到微博，唤起微博分享界面
		mWeiboShareAPI.sendRequest(request);

	}

	/**
	 * 创建图片消息对象。
	 * 
	 * @return 图片消息对象。
	 */
	private ImageObject getImageObj() {
		ImageObject imageObject = new ImageObject();
		BitmapDrawable bitmapDrawable=null;
		if(qrImgImageView!=null&&qrImgImageView.getDrawable()!=null){
			bitmapDrawable = (BitmapDrawable) qrImgImageView.getDrawable();
		}
		else{//如果二维码未生成，直接点击分享到微博按钮，则默认分享本app的logo
			bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher);
		}
		imageObject.setImageObject(bitmapDrawable.getBitmap());
		return imageObject;
	}
	
	/**
	 * 创建 BitMap
	 * @return BitMap
	 */
	private Bitmap getBitmap(){
		BitmapDrawable bitmapDrawable=null;
		if(qrImgImageView!=null&&qrImgImageView.getDrawable()!=null){
			bitmapDrawable = (BitmapDrawable) qrImgImageView.getDrawable();
		}
		else{//如果二维码未生成，直接点击分享到微博按钮，则默认分享本app的logo
			bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher);
		}
	  return bitmapDrawable.getBitmap();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			if (mSsoHandler != null) {
				mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
			}
		}
	}

	/**
	 * 微博认证授权回调类。 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用
	 * {@link SsoHandler#authorizeCallBack} 后， 该回调才会被执行。 2. 非 SSO
	 * 授权时，当授权结束后，该回调就会被执行。 当授权成功后，请保存该 access_token、expires_in、uid 等信息到
	 * SharedPreferences 中。
	 */
	class AuthListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			// 从 Bundle 中解析 Token
			mAccessToken = Oauth2AccessToken.parseAccessToken(values);
			if (mAccessToken.isSessionValid()) {
				// 显示 Token
				// updateTokenView(false);

				// 保存 Token 到 SharedPreferences
				AccessTokenKeeper.writeAccessToken(GenerateQRCodeActivity.this,
						mAccessToken);
				/*
				 * Toast.makeText(GenerateQRCodeActivity.this, "success",
				 * Toast.LENGTH_SHORT).show();
				 */
				sendMessage();
			} else {
				// 以下几种情况，您会收到 Code：
				// 1. 当您未在平台上注册的应用程序的包名与签名时；
				// 2. 当您注册的应用程序包名与签名不正确时；
				// 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
				String code = values.getString("code");
				String message = "failed";
				if (!TextUtils.isEmpty(code)) {
					message = message + "\nObtained the code: " + code;
				}
				Toast.makeText(GenerateQRCodeActivity.this, message,
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onCancel() {
			Toast.makeText(GenerateQRCodeActivity.this,  getString(R.string.app_cancel),
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(GenerateQRCodeActivity.this,
					getString(R.string.auth_fail) + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}
	
	private void sendPictureToWeChat(int flag){
		Bitmap bmp = getBitmap();
		WXImageObject imgObj = new WXImageObject(bmp);
		
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
	//	bmp.recycle();  //不回收，防止以后用到
		msg.thumbData = Util.bmpToByteArray(thumbBmp, false);  //不回收，防止以后用到

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		if(ShareToFriendsCircle==flag){//发送到朋友圈
			req.scene=SendMessageToWX.Req.WXSceneTimeline;
		}else if(ShareToFriend==flag){//发送给朋友
			req.scene=SendMessageToWX.Req.WXSceneSession;
		}
	
		wxApi.sendReq(req);
	}

}