package com.restrofit2demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
public class RxJavaActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView;
    private  int resId=R.mipmap.ic_launcher;
    private List<Student> studentList=new ArrayList<>();
    private Button btn_play,btn_recorder,btn_stop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_java);
        imageView= (ImageView) findViewById(R.id.imageView);
        btn_recorder= (Button) findViewById(R.id.btn_recorder);
        btn_play= (Button) findViewById(R.id.btn_play);
        btn_stop= (Button) findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_recorder.setOnClickListener(this);
        for (int i = 0; i < 4; i++) {
            Student student=new Student();
            student.name="学生"+i;
            student.list=new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                Course course=new Course();
                course.courseName="学生"+i+":课程名"+j;
                student.list.add(course);
            }
            studentList.add(student);
        }
        //  测试1
        Observable observable=Observable.create(new Observable.OnSubscribe<Bitmap>(){
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {//加载图片将会发生在 IO 线程
                //  Toast.makeText(RxJavaActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                subscriber.onStart();
                subscriber.onNext(BitmapFactory.decodeResource(getResources(), resId));
                subscriber.onCompleted();
            }
        });
        /*
         Schedulers.immediate(): 直接在当前线程运行，相当于不指定线程。这是默认的 Scheduler。
          Schedulers.newThread(): 总是启用新线程，并在新线程执行操作。
          Schedulers.io(): I/O 操作（读写文件、读写数据库、网络信息交互等）所使用的 Scheduler
           AndroidSchedulers.mainThread()，它指定的操作将在 Android 主线程运行。

          subscribeOn(): 指定 subscribe() 所发生的线程
          observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程
          */
        observable.subscribeOn(Schedulers.io());// 指定 subscribe() 发生在 IO 线程
        observable.observeOn(AndroidSchedulers.mainThread());// 指定 Subscriber 的回调发生在主线程
        observable.subscribe(new Subscriber<Bitmap>() {
            //默认没自动实现这个方法
            @Override
            public void onStart() {
                Log.e("测试1--->", "开始走你");
            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Bitmap o) {
                Log.e("测试1--->", "设置图片");
                //设置图片则被设定在了主线程
                imageView.setImageBitmap(o);
            }
        });


        //   Observable studentObserVable=
        //测试2
        Observable.from(studentList).map(new Func1<Student, String>() {
            @Override
            public String call(Student student) {
                return student.name;
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.e("测试2--->", s);
            }
        });
        //测试3
        Observable.from(studentList).flatMap(new Func1<Student, Observable<Course>>() {
            @Override
            public Observable<Course> call(Student student) {
                return Observable.from(student.list);
            }
        }).subscribe(new Subscriber<Course>() {
            @Override
            public void onCompleted() {
                //Toast.makeText(RxJavaActivity.this, "Error!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Course course) {
                Log.e("测试3--->", course.courseName);
            }
        });
        //测试4
        Observable.from(studentList).lift(new Observable.Operator<String, Student>() {
            @Override
            public Subscriber<? super Student> call(final Subscriber<? super String> subscriber) {
                return new Subscriber<Student>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Student student) {
                        subscriber.onNext(student.name);
                    }
                };
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.e("测试4--->", s);
            }
        });

        //测试5
        Observable.just("test").map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                //对"test"的数据进行处理
                return s;
            }
        }).subscribeOn(Schedulers.io())
           .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                //拿到call方法对"test"的数据进行处理的结果
            }
        });
   /*     //测试6
        //假设根据输入的字符串获取网址列表
        OkHttpMannager.getInstance().v2EX().query("test").flatMap(new Func1<List<String>, Observable<String>>() {
            @Override
            public Observable<String> call(List<String> strings) {
                return Observable.from(strings);
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                //会把;List<String> 里面的String全部都打印出来
            }
        });
        //测试7
        //获取网址列表之后拿取标题
        OkHttpMannager.getInstance().v2EX().query("test").flatMap(new Func1<List<String>, Observable<String>>() {
            @Override
            public Observable<String> call(List<String> urls) {
                //获取网页列表
                return Observable.from(urls);
            }
        }).flatMap(new Func1<String, Observable<String>>() {
            @Override
            public Observable<String> call(String url) {
                //在根据网页获取标题
                return OkHttpMannager.getInstance().v2EX().getTitle(url);
            }
        }).filter(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String s) {
                //用于判断是否要输出这个值
                if (s==null)
                return false;
                return true;
            }
        }) .take(5)//最多要5个数据
           .subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                //会把;List<String> 里面的String全部都打印出来
            }
        });*/
    }

    @Override
    public void onClick(View v) {

    }
}
