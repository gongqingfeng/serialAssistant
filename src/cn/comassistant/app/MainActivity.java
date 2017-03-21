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
	// �� BluetoothChatService ��������͵���Ϣ����
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
	// Intent request codes �����������
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	// ���ӵ��豸����
	private String mConnectedDeviceName = null;
	// �Ի��߳�����������

	// ���ͷ���
	private BluetoothCarService mCarService = null;
	// ��������������
	final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
			.getDefaultAdapter();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ȥ��״̬��
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
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ʵ��ȫ��

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
			Toast.makeText(MainActivity.this, "�����ѿ���", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MainActivity.this, "����δ����", Toast.LENGTH_SHORT)
					.show();
		}
		if (mCarService == null)
			setupControl();

	}

	// ���´���Ϊ���ð�ť�����¼�����Ӧ��ť���º�����Ϣ

	private void setupControl() {

		// ��ʼ��һ���������򣬵����¼��ķ��Ͱ�ť
		// ��ť����

		// �˵����ܰ�ť
		btnItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// openOptionsMenu(); λ����ʾ����
				showPopupMenu(btnItem); // �ڰ�ť��Χ
			}
		});

		// ���湦�ܰ�ť
		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				WriteFile(textReceive.getText().toString(), "gg");
			}
		});

		// ��ս��������ܰ�ť
		btnClearOn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				textReceive.setText("");
			}
		});

		// ֹͣ��ʾ���ܰ�ť
		btnStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (Flag == 0) {
					btnStop.setText("��ʾ");
					Flag = 1;
				} else {
					btnStop.setText("ֹͣ");
					Flag = 0;
				}
			}
		});

		// ��շ��������ܰ�ť
		btnClearDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				editSend.setText("");
			}
		});

		// ���͹��ܰ�ť
		btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendMessage(editSend.getText().toString());
			}
		});

		// ��ʼ��ִ���������� BluetoothCarService
		mCarService = new BluetoothCarService(this, mHandler);

	}

	// �ǴӸ� ���»�ȡ��Ϣ��ʾ��UI�Ĵ������

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
					equipment.setText("��");
					mTitle.setText(R.string.title_not_connected);
					break;
				case BluetoothCarService.STATE_NONE:
					equipment.setText("��");
					mTitle.setText(R.string.title_not_connected);
					BluetoothAdapter cwjBluetoothAdapter = BluetoothAdapter
							.getDefaultAdapter();

					if (cwjBluetoothAdapter == null) {
						Toast.makeText(MainActivity.this, "����û���ҵ�����Ӳ����������������",
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
						editSend.getText().toString() + "�ѷ���",
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
				// ���������豸����
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				equipment.setText(mConnectedDeviceName);
				Toast.makeText(getApplicationContext(),
						"Connect to  " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				equipment.setText("��");
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
				// ��ȡ�豸��ַ

				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				Toast.makeText(this, "���豸MAC��ַΪ--->" + address,
						Toast.LENGTH_SHORT).show();
				// ��ȡ�豸����

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
				mCarService.connect(device); // ����BluetoothCarService��ķ������������豸
			}

			break;
		case REQUEST_ENABLE_BT:
			// ���������������󷵻�ʱ

			if (resultCode == Activity.RESULT_OK) {
				// �����������ã���ȡԤ��ָ��ֵ
				setupControl();
			} else {
				// �û�û������������������
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				// finish();
			}
		}
	}

	/**
	 * ��������
	 * 
	 * @param i
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// ����Ƿ���������
		if (mCarService.getmState() != BluetoothCarService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		// �����ʲôʵ�ʷ���
		if (message.length() > 0) {
			// ��ȡ��Ϣ�ֽڲ�д�� BluetoothCarService
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
			dlg.setTitle("�������û���");
			dlg.setPositiveButton("����", new DialogInterface.OnClickListener() {

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
			Toast.makeText(getApplicationContext(), "�������Զ��ر�",
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
			Toast.makeText(getApplicationContext(), "û���ļ����Դ洢", 0x0).show();
			return;
		}
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		final EditText editText = new EditText(this);
		dlg.setView(editText);
		dlg.setTitle("�������ļ���");
		dlg.setPositiveButton("����", new DialogInterface.OnClickListener() {

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
						// ��ָ�����ļ����д����ļ�
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
							filename + ".txt" + "�Ѵ洢��sdcard/Data Save��",
							Toast.LENGTH_SHORT).show();
					return;
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),
							filename + ".txt" + "�洢ʧ��", Toast.LENGTH_SHORT)
							.show();
					Log.e("m", "file write error");
				}
			}
		});
		dlg.create();
		dlg.show();
	}

	private void showPopupMenu(View view) {
		// View��ǰPopupMenu��ʾ�����View��λ��
		PopupMenu popupMenu = new PopupMenu(this, view);
		// menu����
		popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
		// menu��item����¼�
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
							dlg.setTitle("�������û���");
							dlg.setPositiveButton("����",
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
		// PopupMenu�ر��¼�
		popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
			@Override
			public void onDismiss(PopupMenu menu) {
				// Toast.makeText(getApplicationContext(), "�ر�PopupMenu",
				// Toast.LENGTH_SHORT).show();
			}
		});

		popupMenu.show();
	}

	public void dialog_Exit(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("ȷ��Ҫ�˳���?");
		builder.setTitle("��ʾ");
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setPositiveButton("�ǳ�ȷ��",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// android.os.Process.killProcess(android.os.Process
						// .myPid());
						Toast.makeText(getApplicationContext(), "�������Զ��ر�",
								Toast.LENGTH_SHORT).show();
						finish();
					}
				});

		builder.setNegativeButton("����ʹ��",
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
