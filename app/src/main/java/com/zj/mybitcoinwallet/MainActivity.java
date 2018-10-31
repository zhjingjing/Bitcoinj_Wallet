package com.zj.mybitcoinwallet;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.util.Date;

public class MainActivity extends Activity {
    private TextView tvAddress;
    private TextView tvFileAddress;
    private TextView tvBtcAddress;
    private TextView tvAount;
    private TextView btnSend_AM;
    private EditText etAmount;
    private TextView tvKey;
    private String addressStr;

    private NetworkParameters parameters;
    private WalletAppKit walletAppKit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAddress=findViewById(R.id.tv_do_address);
        tvFileAddress=findViewById(R.id.tv_file_address);
        tvBtcAddress=findViewById(R.id.tv_btc_address);
        tvAount=findViewById(R.id.tv_amount);
        btnSend_AM=findViewById(R.id.btnSend_AM);
        etAmount=findViewById(R.id.etAmount_AM);
        tvKey=findViewById(R.id.tv_key);



        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file=new File(getCacheDir()+"/"+Constants.WALLET_NAME+".wallet");

                if (file!=null){
                    try {
                        Wallet wallet=Wallet.loadFromFile(file);

                        addressStr= wallet.freshReceiveAddress().toBase58();
                        tvBtcAddress.setText("钱包地址："+ wallet.freshReceiveAddress().toBase58()+"\n"+ wallet.freshReceiveAddress()+"\n");
                        tvFileAddress.setText(".wallet文件地址:"+getCacheDir()+"/"+Constants.WALLET_NAME);
                        tvAount.setText("余额："+wallet.getBalance()+"   ===\n"+ "\n"+wallet.getWatchingKey());

                    } catch (UnreadableWalletException e) {
                        e.printStackTrace();
                    }

                }else{
                setBtcSDKThread();
                ECKey ceKey = new ECKey();
                parameters = Constants.IS_PRODUCTION ? MainNetParams.get() : TestNet3Params.get();


                tvKey.setText("PrivKey：\n"+ceKey.getPrivKey()+"\n\n\n"+"PrivateKeyAsHex：\n" +ceKey.getPrivateKeyAsHex()+"\n\n\n"+"PublicKeyAsHex: \n"+ceKey.getPublicKeyAsHex()+"\n"
                             +  "\n\nbase58编码后的地址：\n"+ceKey.toAddress(parameters).toBase58()
                             +"\n\n\n"
                );
                BriefLogFormatter.init();
                walletAppKit = new WalletAppKit(parameters, getCacheDir(), Constants.WALLET_NAME) {
                    @Override
                    protected void onSetupCompleted() {
//                        if (wallet().getImportedKeys().size() < 1) wallet().importKey(new ECKey());
//                        wallet().allowSpendingUnconfirmedTransactions();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                addressStr= walletAppKit.wallet().freshReceiveAddress().toBase58();
                                tvBtcAddress.setText("钱包地址："+ walletAppKit.wallet().freshReceiveAddress().toBase58()+"\n"+ walletAppKit.wallet().freshReceiveAddress()+"\n"+wallet().freshReceiveAddress().toBase58()+"\n"+wallet().freshReceiveAddress());
                                tvFileAddress.setText(".wallet文件地址:"+vWalletFile.getAbsolutePath());
                                tvAount.setText("余额："+vWallet.getBalance()+"   ===\n"+ walletAppKit.wallet().getWatchingKey()+"\n"+wallet().getWatchingKey());
                            }
                        });
                    }
                };

                walletAppKit.setDownloadListener(new DownloadProgressTracker() {
                    @Override
                    protected void progress(double pct, int blocksSoFar, Date date) {
                        super.progress(pct, blocksSoFar, date);
                        int percentage = (int) pct;
                    }

                    @Override
                    protected void doneDownload() {
                        super.doneDownload();
                    }
                });
                walletAppKit.setBlockingStartup(false);
                walletAppKit.startAsync();
            }
            }

        });


        btnSend_AM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount=etAmount.getText().toString();
                SendRequest request = SendRequest.to(Address.fromBase58(parameters, addressStr), Coin.parseCoin(amount));
                try {
                    walletAppKit.wallet().completeTx(request);
                    walletAppKit.wallet().commitTx(request.tx);
                    walletAppKit.peerGroup().broadcastTransaction(request.tx).broadcast();
                } catch (InsufficientMoneyException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void setBtcSDKThread() {
        final Handler handler = new Handler();
        Threading.USER_THREAD = handler::post;
    }
}
