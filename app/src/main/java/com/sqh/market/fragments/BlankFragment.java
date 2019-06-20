package com.sqh.market.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sqh.market.R;
import com.sqh.market.constant.Constants;
import com.sqh.market.constant.MyConstant;
import com.sqh.market.utils.LoginCheckUtil;
import com.sqh.market.utils.NetUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;


public class BlankFragment extends Fragment implements EasyPermissions.PermissionCallbacks {
    private Context mContext;
    private AlertDialog profilePictureDialog;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int REQUEST_PERMISSION_CAMERA = 0x001;
    private static final int REQUEST_PERMISSION_WRITE = 0x002;
    private static final int CROP_REQUEST_CODE = 0x003;

    private EditText descr;
    private ImageView imageup;
    private EditText goodsName;
    private EditText goodsPrice;
    private Spinner spinner;
    private ArrayAdapter adapter;
    private EditText goodsSum;
    private EditText userPhone;
    private Button submitBt;
    private Bitmap imagebitmap;
    private ImageView ivAvatar;
    private String type;

    /**
     * 文件相关
     */
    private File captureFile;
    private File rootFile;
    private File cropFile;
    private Array instance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank, null);
        mContext = getActivity();
        init(view);
        icient();
        return view;
    }

    private void icient() {
        submitBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Descr;
                String GoodsName;
                String GoodsPrice;
                String GoodSum;
                String UserPone;
                String text;

                Descr = descr.getText().toString();
                GoodsName = goodsName.getText().toString();
                GoodsPrice = goodsPrice.getText().toString();
                GoodSum = goodsSum.getText().toString();
                UserPone = userPhone.getText().toString();
                text = bitmapToBase64(imagebitmap);
                int id = tpyeid(type);

                if(LoginCheckUtil.isLogin(mContext)){
//构造请求体
                    RequestBody body = new FormBody.Builder()
                            .add("commodityName", GoodsName + "")
                            .add("commodityType",  id+ "")
                            .add("commodityInfo", Descr+"")
                            .add("commodityImg", "data:image/jpeg;base64,"+text+"")
                            .add("commodityPrice", GoodSum+"")
                            .add("commodityTotal", GoodSum)
                            .build();
                    //进行网络请求
                    NetUtil.doPost(Constants.BASE_URL + Constants.ADD_COMMODITY_URL
                            , body, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    uiHandler.sendEmptyMessage(-1);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String result = response.body().string().trim();
                                    Message msg = Message.obtain();
                                    JSONObject object = JSON.parseObject(result);
                                    if (object.getBoolean("flag")) {
                                        //请求处理成功且响应成功！
                                        msg.obj = object.getString("message");
                                        msg.what = 4;
                                    } else {
                                        //消息响应成功，但处理失败！
                                        msg.what = 0;
                                    }

                                    //发送handler消息
                                    uiHandler.sendMessage(msg);
                                }
                            });
                }else {
                    uiHandler.sendEmptyMessage(-2);
                }

                System.out.println("详细描述" + Descr);
                System.out.println("商品名称" + GoodsName);
                System.out.println("商品价格" + GoodsPrice);
                System.out.println("商品总量" + GoodSum);
                System.out.println("手机号" + UserPone);
                System.out.println(text);
            }
        });

        imageup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (profilePictureDialog == null) {
                    @SuppressLint("InflateParams") View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_profile_picture, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    rootView.findViewById(R.id.tv_take_photo).setOnClickListener(onTakePhotoListener);
                    rootView.findViewById(R.id.tv_choose_photo).setOnClickListener(onChoosePhotoListener);
                    builder.setView(rootView);
                    profilePictureDialog = builder.create();
                    profilePictureDialog.show();
                } else {
                    profilePictureDialog.show();
                }
            }
        });
    }

    private void init(View view) {
        spinner = (Spinner)view.findViewById(R.id.Spinner01);
        descr = (EditText) view.findViewById(R.id.InfoDes);
        imageup = (ImageView) view.findViewById(R.id.imageUp);
        goodsName = (EditText) view.findViewById(R.id.value);
        goodsPrice = (EditText) view.findViewById(R.id.value2);
        goodsSum = (EditText) view.findViewById(R.id.value3);
        userPhone = (EditText) view.findViewById(R.id.value4);
        submitBt = (Button) view.findViewById(R.id.submit);

        ivAvatar = view.findViewById(R.id.iv_avatar);

        rootFile = new File(MyConstant.PIC_PATH);
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }

        //下拉框选项的方法

        //定义数组，保存省份
        final String[] provinces = {"食品","饮品","3C数码","生活家居","服装服饰","美妆洗护","箱包","母婴","图书","宠物"};
        //创建适配器
        ArrayAdapter adapters = new ArrayAdapter(mContext,android.R.layout.simple_list_item_1,provinces);
        //设置适配器
        spinner.setAdapter(adapters);
        //获取当前选中的选项
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                Toast.makeText(mContext,provinces[position],Toast.LENGTH_SHORT).show();
                type = provinces[position];
            }

            @Override
            public void onNothingSelected(AdapterView parent) {

            }
        });

    }


    private void dismissProfilePictureDialog() {
        if (profilePictureDialog != null && profilePictureDialog.isShowing()) {
            profilePictureDialog.dismiss();
        }
    }

    private View.OnClickListener onTakePhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismissProfilePictureDialog();
            if (EasyPermissions.hasPermissions(mContext, PERMISSION_CAMERA, PERMISSION_WRITE)) {
                takePhoto();
            } else {
                EasyPermissions.requestPermissions(BlankFragment.this, "need camera permission", REQUEST_PERMISSION_CAMERA, PERMISSION_CAMERA, PERMISSION_WRITE);
            }
        }
    };

    //拍照
    private void takePhoto() {
        //用于保存调用相机拍照后所生成的文件
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        captureFile = new File(rootFile, "temp.jpg");
        //跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断版本 如果在Android7.0以上,使用FileProvider获取Uri
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            Uri contentUri = FileProvider.getUriForFile(mContext, "com.sqh.market.fragments", captureFile);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
//        } else
     {
            //否则使用Uri.fromFile(file)方法获取Uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(captureFile));
        }
        startActivityForResult(intent, REQUEST_PERMISSION_CAMERA);
    }

    private View.OnClickListener onChoosePhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismissProfilePictureDialog();
            if (EasyPermissions.hasPermissions(mContext, PERMISSION_WRITE)) {
                choosePhoto();
            } else {
                EasyPermissions.requestPermissions(BlankFragment.this, "need camera permission", REQUEST_PERMISSION_WRITE, PERMISSION_WRITE);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    //从相册选择
    private void choosePhoto() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_PERMISSION_WRITE);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            takePhoto();
        } else if (requestCode == REQUEST_PERMISSION_WRITE) {
            choosePhoto();
        }
    }

    /**
     * 裁剪图片
     */
    private void cropPhoto(Uri uri) {
        cropFile = new File(rootFile, "avatar.jpg");
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cropFile));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra("noFaceDetection", true);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PERMISSION_CAMERA:
                    {
                        cropPhoto(Uri.fromFile(captureFile));
                    }
                    break;
                case REQUEST_PERMISSION_WRITE:
                    cropPhoto(data.getData());
                    break;
                case CROP_REQUEST_CODE:
                    saveImage(cropFile.getAbsolutePath());
                    imagebitmap = BitmapFactory.decodeFile(cropFile.getAbsolutePath());
                    ivAvatar.setImageBitmap(imagebitmap);
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * @param path
     * @return
     */
    public String saveImage(String path) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        try {
            FileOutputStream fos = new FileOutputStream(cropFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return cropFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }



    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    /**
     *    * bitmap??base64   * @param bitmap   * @return   
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void ArrayAdapter() {
        //将可选内容与ArrayAdapter连接起来
        adapter = ArrayAdapter.createFromResource(mContext, R.array.sort, android.R.layout.simple_spinner_item);

        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //将adapter2 添加到spinner中
        spinner.setAdapter(adapter);

        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new SpinnerXMLSelectedListener());

        //设置默认值
        spinner.setVisibility(View.VISIBLE);

    }

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -2:
                    //未登录，需要登录！
                    Toast.makeText(mContext, "您还未登录！请先登录！", Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    //网络请求失败
                    Toast.makeText(mContext, "网络请求失败！"
                            , Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    //网络请求成功，但是返回状态为失败
                    Toast.makeText(mContext, msg.obj == null ? "请求处理失败！"
                            : msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    //添加进购物车回调
                    Toast.makeText(mContext, "添加进购物车" + msg.obj.toString(), Toast.LENGTH_LONG).show();
                    //dismiss掉dialog

                    break;
                case 4:
                    //购买回调
                    Toast.makeText(mContext, "添加商品" + msg.obj.toString(), Toast.LENGTH_LONG).show();
                    //dismiss掉dialog
                    break;
                default:
                    break;
            }
        }
    };
    public int tpyeid(String type) {
        String typelist[]={"食品","饮品","3C数码","生活家居","服装服饰","美妆洗护","箱包","母婴","图书","宠物"};
        for (int i = 0; i < typelist.length-1; i++) {
            if (type == typelist[i]) {
                return i + 1;
            }
        }
        return 0;
    }

}

//使用XML形式操作
class SpinnerXMLSelectedListener implements AdapterView.OnItemSelectedListener {
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
//            view.setText("请选择发布类型："+adapter.getItem(arg2));
    }

    public void onNothingSelected(AdapterView<?> arg0) {

    }
}
