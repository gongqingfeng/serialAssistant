package cn.comassistant.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;
	// 从 BluetoothChatService 处理程序发送的消息类型
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final String PREFS_NAME = "BluetoothCar";
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	public static int Flag = 0;
	String filename;
	TextView mTitle;
	TextView equipment;
	Button btnItem, btnSave, btnClearOn, btnStop, btnClearDown, btnSend;
	TextView textReceive;
	EditText editSend;
	// Intent request codes 意向请求代码
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	// 连接的设备名称
	private String mConnectedDeviceName = null;
	// 对话线程数组适配器

	// 发送服务
	private BluetoothCarService mCarService = null;
	// 本地蓝牙适配器
	final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
			.getDefaultAdapter();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 去掉状态栏
		setContentView(R.layout.newn);
		textReceive = (TextView) findViewById(R.id.TV_RECEIVE);
		textReceive.setMovementMethod(ScrollingMovementMethod.getInstance());
		mTitle = (TextView) findViewById(R.id.mtitle);
		equipment = (TextView) findViewById(R.id.equipment);
		btnItem = (Button) findViewById(R.id.BTN_ITEM);
		btnSave = (Button) findViewById(R.id.BTN_SAVE);
		btnClearOn = (Button) findViewById(R.id.BTN_CLEAR);
		btnStop = (Button) findViewById(R.id.BTN_STOP);
		btnClearDown = (Button) findViewById(R.id.BTN_CLEAR_DOWN);
		btnSend = (Button) findViewById(R.id.BTN_SEND);
		editSend = (EditText) findViewById(R.id.ET_SEND);
		equipment = (TextView) findViewById(R.id.equipment);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 实现全屏

		if (bluetoothAdapter == null) {

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("No bluetooth devices");
			dialog.setMessage("Your equipment does not support bluetooth, please change device");

			dialog.setNegativeButton("cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			dialog.show();
		}
		if (bluetoothAdapter.isEnabled()) {
			Toast.makeText(MainActivity.this, "蓝牙已开启", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MainActivity.this, "蓝牙未开启", Toast.LENGTH_SHORT)
					.show();
		}
		if (mCarService == null)
			setupControl();

	}

	// 以下代码为设置按钮监听事件，响应按钮按下后发送消息

	private void setupControl() {

		// 初始化一个侦听程序，单击事件的发送按钮
		// 按钮代码

		// 菜单功能按钮
		btnItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// openOptionsMenu(); 位置显示不好
				showPopupMenu(btnItem); // 在按钮周围
			}
		});

		// 保存功能按钮
		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				WriteFile(textReceive.getText().toString(), "gg");
			}
		});

		// 清空接收区功能按钮
		btnClearOn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				textReceive.setText("");
			}
		});

		// 停止显示功能按钮
		btnStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (Flag == 0) {
					btnStop.setText("显示");
					Flag = 1;
				} else {
					btnStop.setText("停止");
					Flag = 0;
				}
			}
		});

		// 清空发送区功能按钮
		btnClearDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				editSend.setText("");
			}
		});

		// 发送功能按钮
		btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendMessage(editSend.getText().toString());
			}
		});

		// 初始化执行蓝牙连接 BluetoothCarService
		mCarService = new BluetoothCarService(this, mHandler);

	}

	// 是从该 重新获取信息显示在UI的处理程序

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothCarService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					// mTitle.append(mConnectedDeviceName);
					// mConversationArrayAdapter.clear();
					break;
				case BluetoothCarService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothCarService.STATE_LISTEN:
					equipment.setText("无");
					mTitle.setText(R.string.title_not_connected);
					break;
				case BluetoothCarService.STATE_NONE:
					equipment.setText("无");
					mTitle.setText(R.string.title_not_connected);
					BluetoothAdapter cwjBluetoothAdapter = BluetoothAdapter
							.getDefaultAdapter();

					if (cwjBluetoothAdapter == null) {
						Toast.makeText(MainActivity.this, "本机没有找到蓝牙硬件或驱动存在问题",
								Toast.LENGTH_SHORT).show();
					}
					if (!cwjBluetoothAdapter.isEnabled()) {

						Intent TurnOnBtIntent = new Intent(
								BluetoothAdapter.ACTION_REQUEST_ENABLE);

						startActivityForResult(TurnOnBtIntent,
								REQUEST_ENABLE_BT);
					}
					break;
				}
				break;
			case MESSAGE_WRITE:
				Toast.makeText(getApplicationContext(),
						editSend.getText().toString() + "已发送",
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_READ:
				if (Flag == 0) {
					byte[] readBuf = (byte[]) msg.obj;
					String readMessage = new String(readBuf, 0, msg.arg1);
					if (Flag == 0) {
						// textReceive.setText(textReceive.getText().toString()+readMessage);
						refreshTextView(textReceive, readMessage);
					}
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// 保存连接设备名称
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				equipment.setText(mConnectedDeviceName);
				Toast.makeText(getApplicationContext(),
						"Connect to  " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				equipment.setText("无");
				mTitle.setText(R.string.title_not_connected);
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				// 获取设备地址

				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				Toast.makeText(this, "该设备MAC地址为--->" + address,
						Toast.LENGTH_SHORT).show();
				// 获取设备对象

				BluetoothDevice device = bluetoothAdapter
						.getRemoteDevice(address);
				UUID uuid = UUID
						.fromString("00001101-0000-1000-8000-00805f9b34fb");
				try {
					device.createRfcommSocketToServiceRecord(uuid);

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// Attempt to connect to the device
				mCarService.connect(device); // 调用BluetoothCarService类的方法进行连接设备
			}

			break;
		case REQUEST_ENABLE_BT:
			// 当启用蓝牙的请求返回时

			if (resultCode == Activity.RESULT_OK) {
				// 蓝牙现已启用，读取预设指令值
				setupControl();
			} else {
				// 用户没有启用蓝牙或发生错误
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				// finish();
			}
		}
	}

	/**
	 * 发送数据
	 * 
	 * @param i
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// 检查是否真正连上
		if (mCarService.getmState() != BluetoothCarService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		// 检查有什么实际发送
		if (message.length() > 0) {
			// 获取消息字节并写入 BluetoothCarService
			byte[] send = message.getBytes();
			mCarService.write(send);
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.str:
			Intent intent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
			break;

		case R.id.str2:
			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
			final EditText editText = new EditText(this);
			dlg.setView(editText);
			dlg.setTitle("请输入用户名");
			dlg.setPositiveButton("设置", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if (editText.getText().toString().length() != 0)
						bluetoothAdapter.setName(editText.getText().toString());

				}
			});
			dlg.create();
			dlg.show();
			break;
		case R.id.str3:
			Intent intent1 = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			intent1.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(intent1);
			break;

		case R.id.strOn:
			if (!bluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			}
			break;

		case R.id.strOff:

			if (bluetoothAdapter.isEnabled()) {
				bluetoothAdapter.disable();
			}

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.disable();
			Toast.makeText(getApplicationContext(), "蓝牙已自动关闭",
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (((keyCode == KeyEvent.KEYCODE_BACK) || (keyCode == KeyEvent.KEYCODE_HOME))
				&& event.getRepeatCount() == 0) {
			dialog_Exit(MainActivity.this);
		}
		return false;

		// end onKeyDown
	}

	public void WriteFile(final String s, String name) {
		if (s.length() == 0) {
			Toast.makeText(getApplicationContext(), "没有文件可以存储", 0x0).show();
			return;
		}
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		final EditText editText = new EditText(this);
		dlg.setView(editText);
		dlg.setTitle("请输入文件名");
		dlg.setPositiveButton("保存", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (editText.getText().toString().length() != 0)
					filename = editText.getText().toString();
				File destDir = new File("/sdcard/Data Save");
				if (!destDir.exists()) {
					destDir.mkdirs();
				}

				File dir = new File(destDir.getAbsolutePath() + "/" + filename
						+ ".txt");
				if (!dir.exists()) {
					try {
						// 在指定的文件夹中创建文件
						dir.createNewFile();
					} catch (Exception e) {
					}
				}
				try {
					FileOutputStream outStream = new FileOutputStream(dir);
					OutputStreamWriter writer = new OutputStreamWriter(
							outStream);
					writer.write(s);
					writer.flush();
					writer.close();
					outStream.close();

					Toast.makeText(getApplicationContext(),
							filename + ".txt" + "已存储到sdcard/Data Save中",
							Toast.LENGTH_SHORT).show();
					return;
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),
							filename + ".txt" + "存储失败", Toast.LENGTH_SHORT)
							.show();
					Log.e("m", "file write error");
				}
			}
		});
		dlg.create();
		dlg.show();
	}

	private void showPopupMenu(View view) {
		// View当前PopupMenu显示的相对View的位置
		PopupMenu popupMenu = new PopupMenu(this, view);
		// menu布局
		popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
		// menu的item点击事件
		popupMenu
				.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						// Toast.makeText(getApplicationContext(),
						// item.getTitle(), Toast.LENGTH_SHORT).show();
						switch (item.getItemId()) {
						case R.id.str:
							Intent intent = new Intent(MainActivity.this,
									DeviceListActivity.class);
							startActivityForResult(intent,
									REQUEST_CONNECT_DEVICE);
							break;

						case R.id.str2:
							AlertDialog.Builder dlg = new AlertDialog.Builder(
									MainActivity.this);
							final EditText editText = new EditText(
									MainActivity.this);
							dlg.setView(editText);
							dlg.setTitle("请输入用户名");
							dlg.setPositiveButton("设置",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											if (editText.getText().toString()
													.length() != 0)
												bluetoothAdapter
														.setName(editText
																.getText()
																.toString());

										}
									});
							dlg.create();
							dlg.show();
							break;
						case R.id.str3:
							Intent intent1 = new Intent(
									BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
							intent1.putExtra(
									BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
									300);
							startActivity(intent1);
							break;

						case R.id.strOn:
							if (!bluetoothAdapter.isEnabled()) {
								Intent enableIntent = new Intent(
										BluetoothAdapter.ACTION_REQUEST_ENABLE);
								startActivityForResult(enableIntent,
										REQUEST_ENABLE_BT);
							}
							break;

						case R.id.strOff:

							if (bluetoothAdapter.isEnabled()) {
								bluetoothAdapter.disable();
							}

							break;
						}

						return false;
					}
				});
		// PopupMenu关闭事件
		popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
			@Override
			public void onDismiss(PopupMenu menu) {
				// Toast.makeText(getApplicationContext(), "关闭PopupMenu",
				// Toast.LENGTH_SHORT).show();
			}
		});

		popupMenu.show();
	}

	public void dialog_Exit(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("确定要退出吗?");
		builder.setTitle("提示");
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setPositiveButton("非常确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// android.os.Process.killProcess(android.os.Process
						// .myPid());
						Toast.makeText(getApplicationContext(), "蓝牙已自动关闭",
								Toast.LENGTH_SHORT).show();
						finish();
					}
				});

		builder.setNegativeButton("继续使用",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builder.create().show();
	}

	public void refreshTextView(TextView view, String str) {
		view.append(str);
		int offset = view.getLineCount() * view.getLineHeight();
		if (offset > view.getHeight()) {
			view.scrollTo(0, offset - view.getHeight());
		}

	}

}
