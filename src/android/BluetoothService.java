package com.josservices.cordova.plugins.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class BluetoothService
{
  private static final String TAG = "BluetoothService";
  private static final boolean D = true;
  public static final int MESSAGE_STATE_CHANGE = 1;
  public static final int MESSAGE_READ = 2;
  public static final int MESSAGE_WRITE = 3;
  public static final int MESSAGE_DEVICE_NAME = 4;
  public static final int MESSAGE_CONNECTION_LOST = 5;
  public static final int MESSAGE_UNABLE_CONNECT = 6;
  private static final String NAME = "BTPrinter";
  private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  private final BluetoothAdapter mAdapter;
  private final Handler mHandler;
  private AcceptThread mAcceptThread;
  private ConnectThread mConnectThread;
  private ConnectedThread mConnectedThread;
  private int mState;
  public static final int STATE_NONE = 0;
  public static final int STATE_LISTEN = 1;
  public static final int STATE_CONNECTING = 2;
  public static final int STATE_CONNECTED = 3;

  public BluetoothService(Context context, Handler handler)
  {
    this.mAdapter = BluetoothAdapter.getDefaultAdapter();
    this.mState = 0;
    this.mHandler = handler;
  }

  public synchronized boolean isAvailable()
  {
/*  70 */     if (this.mAdapter == null) {
/*  71 */       return false;
    }
/*  73 */     return true;
  }

  public synchronized boolean isBTopen()
  {
/*  78 */     if (!this.mAdapter.isEnabled()) {
/*  79 */       return false;
    }
/*  81 */     return true;
  }

  public synchronized BluetoothDevice getDevByMac(String mac)
  {
/*  87 */     return this.mAdapter.getRemoteDevice(mac);
  }

  public synchronized BluetoothDevice getDevByName(String name)
  {
/*  92 */     BluetoothDevice tem_dev = null;
/*  93 */     Set<BluetoothDevice> pairedDevices = getPairedDev();
/*  94 */     if (pairedDevices.size() > 0) {
/*  95 */       for (BluetoothDevice device : pairedDevices) {
/*  96 */         if (device.getName().indexOf(name) != -1) {
/*  97 */           tem_dev = device;
/*  98 */           break;
        }
      }
    }
/* 102 */     return tem_dev;
  }

  public synchronized void sendMessage(String message, String charset) {
/* 106 */     if (message.length() > 0)
    {
      byte[] send;
      try {
/* 110 */         send = message.getBytes(charset);
      }
      catch (UnsupportedEncodingException e)
      {
/* 114 */         send = message.getBytes();
      }

/* 117 */       write(send);
/* 118 */       byte[] tail = new byte[3];
/* 119 */       tail[0] = 10;
/* 120 */       tail[1] = 13;
/* 121 */       write(tail);
    }
  }

  public synchronized Set<BluetoothDevice> getPairedDev()
  {
/* 127 */     Set dev = null;
/* 128 */     dev = this.mAdapter.getBondedDevices();
/* 129 */     return dev;
  }

  public synchronized boolean cancelDiscovery()
  {
/* 134 */     return this.mAdapter.cancelDiscovery();
  }

  public synchronized boolean isDiscovering()
  {
/* 139 */     return this.mAdapter.isDiscovering();
  }

  public synchronized boolean startDiscovery()
  {
/* 144 */     return this.mAdapter.startDiscovery();
  }

  private synchronized void setState(int state)
  {
/* 150 */     this.mState = state;

/* 153 */     this.mHandler.obtainMessage(1, state, -1).sendToTarget();
  }

  public synchronized int getState()
  {
/* 159 */     return this.mState;
  }

  public synchronized void start()
  {
/* 167 */     Log.d("BluetoothService", "start");

/* 170 */     if (this.mConnectThread != null) { this.mConnectThread.cancel(); this.mConnectThread = null;
    }

/* 173 */     if (this.mConnectedThread != null) { this.mConnectedThread.cancel(); this.mConnectedThread = null;
    }

/* 176 */     if (this.mAcceptThread == null) {
/* 177 */       this.mAcceptThread = new AcceptThread();
/* 178 */       this.mAcceptThread.start();
    }
/* 180 */     setState(1);
  }

  public synchronized void connect(BluetoothDevice device)
  {
/* 188 */     Log.d("BluetoothService", "connect to: " + device);

/* 191 */     if ((this.mState == 2) && 
/* 192 */       (this.mConnectThread != null)) { this.mConnectThread.cancel(); this.mConnectThread = null;
    }

/* 196 */     if (this.mConnectedThread != null) { this.mConnectedThread.cancel(); this.mConnectedThread = null;
    }

/* 199 */     this.mConnectThread = new ConnectThread(device);
/* 200 */     this.mConnectThread.start();
/* 201 */     setState(2);
  }

  public synchronized void connected(BluetoothSocket socket, BluetoothDevice device)
  {
/* 210 */     Log.d("BluetoothService", "connected");

/* 213 */     if (this.mConnectThread != null) { this.mConnectThread.cancel(); this.mConnectThread = null;
    }

/* 216 */     if (this.mConnectedThread != null) { this.mConnectedThread.cancel(); this.mConnectedThread = null;
    }

/* 219 */     if (this.mAcceptThread != null) { this.mAcceptThread.cancel(); this.mAcceptThread = null;
    }

/* 222 */     this.mConnectedThread = new ConnectedThread(socket);
/* 223 */     this.mConnectedThread.start();

/* 226 */     Message msg = this.mHandler.obtainMessage(4);

/* 228 */     this.mHandler.sendMessage(msg);

/* 230 */     setState(3);
  }

  public synchronized void stop()
  {
/* 237 */     Log.d("BluetoothService", "stop");
/* 238 */     setState(0);
/* 239 */     if (this.mConnectThread != null) { this.mConnectThread.cancel(); this.mConnectThread = null; }
/* 240 */     if (this.mConnectedThread != null) { this.mConnectedThread.cancel(); this.mConnectedThread = null; }
/* 241 */     if (this.mAcceptThread != null) { this.mAcceptThread.cancel(); this.mAcceptThread = null;
    }
  }

  public void write(byte[] out)
  {
    ConnectedThread r;
/* 253 */     synchronized (this) {
/* 254 */       if (this.mState != 3) return;
/* 255 */       r = this.mConnectedThread;
    }
/* 258 */     r.write(out);
  }

  private void connectionFailed()
  {
/* 265 */     setState(1);

/* 268 */     Message msg = this.mHandler.obtainMessage(6);
/* 269 */     this.mHandler.sendMessage(msg);
  }

  private void connectionLost()
  {
/* 279 */     Message msg = this.mHandler.obtainMessage(5);
/* 280 */     this.mHandler.sendMessage(msg);
  }

  private class AcceptThread extends Thread
  {
    private final BluetoothServerSocket mmServerSocket;

    public AcceptThread()
    {
/* 293 */       BluetoothServerSocket tmp = null;
      try
      {
/* 297 */         tmp = BluetoothService.this.mAdapter.listenUsingRfcommWithServiceRecord("BTPrinter", BluetoothService.MY_UUID);
      } catch (IOException e) {
/* 299 */         Log.e("BluetoothService", "listen() failed", e);
      }
/* 301 */       this.mmServerSocket = tmp;
    }

    public void run()
    {
/* 306 */       Log.d("BluetoothService", "BEGIN mAcceptThread" + this);
/* 307 */       setName("AcceptThread");
/* 308 */       BluetoothSocket socket = null;

/* 311 */       while (BluetoothService.this.mState != 3) {
/* 312 */         Log.d("AcceptThread线程运行", "正在运行......");
        try
        {
/* 316 */           socket = this.mmServerSocket.accept();
        } catch (IOException e) {
/* 318 */           Log.e("BluetoothService", "accept() failed", e);
/* 319 */           break;
        }

/* 323 */         if (socket != null) {
/* 324 */           synchronized (BluetoothService.this) {
/* 325 */             switch (BluetoothService.this.mState)
            {
            case 1:
            case 2:
/* 329 */               BluetoothService.this.connected(socket, socket.getRemoteDevice());
/* 330 */               break;
            case 0:
            case 3:
              try
              {
/* 335 */                 socket.close();
              } catch (IOException e) {
/* 337 */                 Log.e("BluetoothService", "Could not close unwanted socket", e);
              }
            }
          }
        }
      }

/* 344 */       Log.i("BluetoothService", "END mAcceptThread");
    }

    public void cancel() {
/* 348 */       Log.d("BluetoothService", "cancel " + this);
      try {
/* 350 */         this.mmServerSocket.close();
      } catch (IOException e) {
/* 352 */         Log.e("BluetoothService", "close() of server failed", e);
      }
    }
  }

  private class ConnectThread extends Thread
  {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public ConnectThread(BluetoothDevice device)
    {
/* 368 */       this.mmDevice = device;
/* 369 */       BluetoothSocket tmp = null;
      try
      {
/* 374 */         tmp = device.createRfcommSocketToServiceRecord(BluetoothService.MY_UUID);
      } catch (IOException e) {
/* 376 */         Log.e("BluetoothService", "create() failed", e);
      }
/* 378 */       this.mmSocket = tmp;
    }

    public void run() {
/* 382 */       Log.i("BluetoothService", "BEGIN mConnectThread");
/* 383 */       setName("ConnectThread");

/* 386 */       BluetoothService.this.mAdapter.cancelDiscovery();
      try
      {
/* 392 */         this.mmSocket.connect();
      } catch (IOException e) {
/* 394 */         BluetoothService.this.connectionFailed();
        try
        {
/* 397 */           this.mmSocket.close();
        } catch (IOException e2) {
/* 399 */           Log.e("BluetoothService", "unable to close() socket during connection failure", e2);
        }

/* 402 */         BluetoothService.this.start();
/* 403 */         return;
      }

/* 407 */       synchronized (BluetoothService.this) {
/* 408 */         BluetoothService.this.mConnectThread = null;
      }

/* 412 */       BluetoothService.this.connected(this.mmSocket, this.mmDevice);
    }

    public void cancel() {
      try {
/* 417 */         this.mmSocket.close();
      } catch (IOException e) {
/* 419 */         Log.e("BluetoothService", "close() of connect socket failed", e);
      }
    }
  }

  private class ConnectedThread extends Thread
  {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket)
    {
/* 434 */       Log.d("BluetoothService", "create ConnectedThread");
/* 435 */       this.mmSocket = socket;
/* 436 */       InputStream tmpIn = null;
/* 437 */       OutputStream tmpOut = null;
      try
      {
/* 441 */         tmpIn = socket.getInputStream();
/* 442 */         tmpOut = socket.getOutputStream();
      } catch (IOException e) {
/* 444 */         Log.e("BluetoothService", "temp sockets not created", e);
      }

/* 447 */       this.mmInStream = tmpIn;
/* 448 */       this.mmOutStream = tmpOut;
    }

    public void run() {
/* 452 */       Log.d("ConnectedThread线程运行", "正在运行......");
/* 453 */       Log.i("BluetoothService", "BEGIN mConnectedThread");
      try
      {
        while (true)
        {
/* 459 */           byte[] buffer = new byte[256];

/* 461 */           int bytes = this.mmInStream.read(buffer);
/* 462 */           if (bytes <= 0) {
            break;
          }
/* 465 */           BluetoothService.this.mHandler.obtainMessage(2, bytes, -1, buffer)
/* 466 */             .sendToTarget();
        }

/* 470 */         Log.e("BluetoothService", "disconnected");
/* 471 */         BluetoothService.this.connectionLost();

/* 474 */         if (BluetoothService.this.mState != 0)
        {
/* 476 */           Log.e("BluetoothService", "disconnected");

/* 478 */           BluetoothService.this.start();
        }
      }
      catch (IOException e)
      {
/* 483 */         Log.e("BluetoothService", "disconnected", e);
/* 484 */         BluetoothService.this.connectionLost();

/* 487 */         if (BluetoothService.this.mState != 0)
        {
/* 490 */           BluetoothService.this.start();
        }
      }
    }

    public void write(byte[] buffer)
    {
      try
      {
/* 503 */         this.mmOutStream.write(buffer);

/* 506 */         BluetoothService.this.mHandler.obtainMessage(3, -1, -1, buffer)
/* 507 */           .sendToTarget();
      } catch (IOException e) {
/* 509 */         Log.e("BluetoothService", "Exception during write", e);
      }
    }

    public void cancel() {
      try {
/* 515 */         this.mmSocket.close();
      } catch (IOException e) {
/* 517 */         Log.e("BluetoothService", "close() of connect socket failed", e);
      }
    }
  }
}

/* Location:           btsdk.jar
 * Qualified Name:     com.zj.btsdk.BluetoothService
 * JD-Core Version:    0.6.2
 */