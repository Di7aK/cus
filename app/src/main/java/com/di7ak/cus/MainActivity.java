package com.di7ak.cus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.di7ak.kolyaloh.anticaptcha.Anticaptcha;
import com.di7ak.kolyaloh.anticaptcha.AnticaptchaException;
import com.di7ak.nn.Layer;
import com.di7ak.spaces.api.Auth;
import com.di7ak.spaces.api.Comm;
import com.di7ak.spaces.api.Mail;
import com.di7ak.spaces.api.Session;
import com.di7ak.spaces.api.SpacesException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener,
DialogInterface.OnClickListener {
    List<Session> sessions;
    ProgressDialog progress;
    boolean canceled;
    Comm comm;
    String[] accounts;
    String message;
    String commName;
    int currentSession;
    int currentUser;
    int totalSended;
    Comm.CommResult users;
    Layer layer;
    String captcha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.btn_begin).setOnClickListener(this);

        loadLayer();
    }

    private void loadLayer() {
        progress = new ProgressDialog(this);
        progress.setTitle("wait");
        progress.setMessage("initializing anticaptcha");
        progress.setCancelable(false);
        progress.show();
        new Thread(new Runnable() {

                @Override
                public void run() {
                    layer = new Layer(12 * 18);
                    try {
                        layer.load(getAssets().open("lol.ns"));
                        runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    progress.hide();
                                }
                            });
                    } catch (IOException e) {
                        showError("failed load layer data");
                    }
                }
            }).start();
    }

    @Override
    public void onClick(View v) {
        progress = new ProgressDialog(this);
        progress.setTitle("progress");
        progress.setCancelable(false);
        progress.setButton("Cancell", this);
        progress.show();

        sessions = new ArrayList<Session>();
        canceled = false;
        totalSended = 0;
        users = null;

        accounts = ((EditText)findViewById(R.id.accounts)).getText().toString().split("\n");
        commName = ((EditText)findViewById(R.id.commName)).getText().toString();
        message = ((EditText)findViewById(R.id.message)).getText().toString();

        getComm();
    }

    @Override
    public void onClick(DialogInterface dialog, int i) {
        canceled = true;
    }

    private void getComm() {
        new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        setMessage("getting comm");
                        comm = Comm.getByAddress(commName);
                        auth();
                    } catch (SpacesException e) {
                        if (e.code == -1 && !canceled) getComm();
                        else showError(e.getMessage());
                    }
                }
            }).start();
    }

    private void auth() {
        new Thread(new Runnable() {

                @Override
                public void run() {
                    for (String account : accounts) {
                        try {
                            if (canceled) return;
                            setMessage(account);

                            String[] accountData = account.split(":");
                            if (accountData.length != 2) continue;

                            addSession(Auth.login(accountData[0], accountData[1]));

                            Thread.sleep(200);
                        } catch (SpacesException e) {
                            if (e.code == 1) break;
                        } catch (InterruptedException e) {
                        }
                    }
                    if (sessions.size() == 0) showError("need accounts");
                    else nextMessage();
                }
            }).start();
    }

    private void addSession(Session session) {
        if (sessions != null) sessions.add(session);
    }

    private void nextMessage() {
        if (canceled) return;
        new Thread(new Runnable() {

                @Override
                public void run() {
                    if (users == null) {
                        setMessage("getting users");
                        try {
                            users = comm.getUsers(1);
                            currentUser = 0;
                        } catch (SpacesException e) {
                            nextMessage();
                            return;
                        }
                    }
                    if (currentUser == users.pagination.itemsOnPage) {
                        if (users.pagination.currentPage < users.pagination.lastPage) {
                            try {
                                setMessage("getting next users");
                                users = comm.getUsers(users.pagination.currentPage + 1);
                                currentUser = 0;
                            } catch (SpacesException e) {
                                nextMessage();
                                return;
                            }
                        } else {
                            showError("completed");
                            return;
                        }
                    }

                    if (currentSession >= sessions.size()) {
                        showError("completed");
                        return;
                    }
                    try {
                        setMessage("sending to " + users.users.get(currentUser).name);
                        Mail.sendMessage(sessions.get(currentSession), users.users.get(currentUser), message, captcha);
                        currentUser ++;
                        totalSended ++;
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {}
                        nextMessage();
                    } catch (SpacesException e) {
                        if (e.code == 1 || e.code == 4) {
                            try {
                                setMessage("handling captcha");
                                URL url = new URL(e.captchaUrl);
                                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                captcha = Anticaptcha.handle(image, layer);
                                Thread.sleep(300);
                            } catch (AnticaptchaException ae) {

                            } catch (Exception ae) {

                            }
                        } else if(e.code == 2003 || e.code == 2009 || e.code == 1008) {
                            currentSession ++;
                        } else if (e.code != -1) {
                            android.util.Log.d("lol", "code: " + e.code);
                            currentUser ++;
                        }
                        nextMessage();
                    }
                }
            }).start();
    }

    private void setMessage(final String message) {
        runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (progress != null && progress.isShowing()) {
                        progress.setMessage(message + ", total sended: " + totalSended);
                    }
                }
            });
    }

    private void showError(final String error) {
        runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (progress != null && progress.isShowing()) {
                        progress.hide();
                    }

                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle("error")
                        .setMessage(error + ", total sended: " + totalSended)
                        .show();
                }
            });
    }
}
