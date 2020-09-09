package com.example.customgridview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.List;

/**
 *  @Author wonderful
 *  @Date 2020-8-12
 *  @Version 1.0
 *  @Description 自定义gridView,主要用于排列规整的场景，支持点击模式、单选模式、多选模式
 *  TODO 颜色默认值不要设置为-1,因为-1会出现在正常范围的颜色值里
 */
public class WonderfulGridView extends SimpleCustomGridView{

    //不允许的颜色值，即颜色设置成这个值将不会起作用
    protected final int forbiddenColor = -111;

    //背景颜色
    protected int selectColor;
    protected int defaultColor;

    //字体颜色
    protected int selectTextColor;
    protected int defaultTextColor;
    //颜色选择器
    protected int colorSelector;

    //响应模式 0：点击模式 1：单选模式 2：多选模式
    protected int responseMode;
    //选择的位置
    //key:选中的位置，value：是否选中
    protected SparseArray<Boolean> choosePosition;

    private SelectChangeListener selectChangeListener;

    public WonderfulGridView(Context context) {
        this(context,null);
    }

    public WonderfulGridView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public WonderfulGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    //初始化
    @SuppressLint("ResourceAsColor")
    private void init(Context context, AttributeSet attrs){

        choosePosition = new SparseArray<>();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.wonderfulGridViewStyle);

        responseMode = typedArray.getInteger(R.styleable.wonderfulGridViewStyle_responseMode,0);

        selectColor = typedArray.getColor(R.styleable.wonderfulGridViewStyle_selectColor,forbiddenColor);
        defaultColor = typedArray.getColor(R.styleable.wonderfulGridViewStyle_defaultColor,forbiddenColor);
        colorSelector = typedArray.getResourceId(R.styleable.wonderfulGridViewStyle_colorSelector,R.drawable.color_selector);

        selectTextColor = typedArray.getColor(R.styleable.wonderfulGridViewStyle_selectTextColor, Color.BLUE);
        defaultTextColor = typedArray.getColor(R.styleable.wonderfulGridViewStyle_defaultTextColor, Color.BLACK);

        typedArray.recycle();
    }

    /**
     * 添加子view
     * @param width 子view的宽
     * @param height 子view的高
     */
    @Override
    protected void addChildrenView(int width, int height, int maxLine) {
        removeAllViews();
        for (int i=0; i<children.size(); i++){
            final String child = children.get(i);

            TextView textView = new TextView(context);
            if (maxLine >0){
                textView.setMaxLines(maxLine);
            }
            textView.setLayoutParams(new ViewGroup.LayoutParams(width, height));

            //如果是点击模式，直接设置选择器即可
            if (responseMode == 0){
                //设置背景选择器
                StateListDrawable drawable = new StateListDrawable();
                Drawable drawableSelect = ContextCompat.getDrawable(context,selectDrawable);
                Drawable drawableNormal = ContextCompat.getDrawable(context,defaultDrawable);
                drawable.addState(new int[]{android.R.attr.state_pressed},drawableSelect);//选中
                drawable.addState(new int[]{},drawableNormal);//未选中
                textView.setBackground(drawable);
                //设置字体颜色选择器
                textView.setTextColor(ContextCompat.getColorStateList(context, colorSelector));
            }
            //否则就是单选或多选模式，需要设置选中的颜色
            else {
                //设置背景drawable,
                if (defaultDrawable != -1){
                    textView.setBackgroundResource(defaultDrawable);
                }
                //设置背景颜色，优先级更高，如果都设置了则会覆盖drawable
                if (defaultColor != forbiddenColor){
                    textView.setBackgroundColor(defaultColor);
                }
                //设置字体颜色
                if (defaultTextColor != forbiddenColor){
                    textView.setTextColor(color(defaultTextColor));
                }

                //设置当前选中的背景、颜色
                //这很重要，因为当一个View由GONE变为VISIBLE的时候会触发onMeasure方法
                //而onMeasure会多次调用addChildrenView，这意味着childView会被重新创建
                //如果choosePosition.get(i)==true说明之前就已经做过选择操作，因此需要设置当前childView为选中状态
                if (choosePosition.get(i) != null && choosePosition.get(i)){
                    //设置背景
                    if (selectDrawable != -1){
                        textView.setBackgroundResource(selectDrawable);
                    }
                    if(selectColor != forbiddenColor){
                        textView.setBackgroundColor(selectColor);
                    }
                    //设置字体颜色
                    if (selectTextColor != forbiddenColor){
                        textView.setTextColor(color(selectTextColor));
                    }
                }
            }
            //设置padding
            if (gridPadding != -1){
                textView.setPadding(gridPadding,gridPadding,gridPadding,gridPadding);
            }else {
                textView.setPadding(leftPadding,topPadding,rightPadding,bottomPadding);
            }
            //居中显示
            textView.setGravity(Gravity.CENTER);
            //设置字体大小
            if (textSize != -1){
                textView.setTextSize(textSize);
            }
            //设置字体内容
            textView.setText(child);
            //设置点击监听事件
            final int finalI = i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(finalI,child);
                }
            });

            addView(textView);
        }
    }

    @Override
    protected void onItemClick(int position, String content) {
        //如果不是点击模式，则选中状态取反
        if (responseMode !=0 ){
            //如果已经是选中状态则变为非选中状态
            if (choosePosition.get(position) != null && choosePosition.get(position)){
                clearSelectItem(position);
                if (selectChangeListener != null){
                    selectChangeListener.unSelect(position,content);
                }
            }
            //否则变为选中状态
            else {
                setSelectItem(position);
                if (selectChangeListener != null){
                    selectChangeListener.onSelect(position,content);
                }
            }
        }
        super.onItemClick(position, content);
    }

    //设置选中项，如果当前View有GONE和VISIBLE的操作，请使用具有二参的重载函数
    public void setSelectItem(int position){
        //点击模式下将不会有任何作用
        if (responseMode == 0){
            //TODO
        }
        //单选模式
        else if (responseMode == 1){
            //先清除所有选中的背景、颜色
            clearAll();
            //设置当前选中的背景、颜色
            TextView textView = (TextView) getChildAt(position);
            if (textView != null){
                //设置背景
                if (selectDrawable != -1){
                    textView.setBackgroundResource(selectDrawable);
                }
                if(selectColor != forbiddenColor){
                    textView.setBackgroundColor(selectColor);
                }
                //设置字体颜色
                if (selectTextColor != forbiddenColor){
                    textView.setTextColor(color(selectTextColor));
                }
            }
        }
        //多选模式
        else if (responseMode == 2){
            //设置当前选中的背景、颜色
            TextView textView = (TextView) getChildAt(position);
            if (textView != null){
                //设置背景
                if (selectDrawable != -1){
                    textView.setBackgroundResource(selectDrawable);
                }
                if(selectColor != forbiddenColor){
                    textView.setBackgroundColor(selectColor);
                }
                //设置字体颜色
                if (selectTextColor != forbiddenColor){
                    textView.setTextColor(color(selectTextColor));
                }
            }
        }
        //保存选中的位置
        choosePosition.put(position,true);
    }

    //设置选中项,item:item的内容，
    public void setSelectItem(String item){
        if (item == null)return;
        for (int i=0; i<children.size(); i++){
            if (children.get(i).equals(item)){
                setSelectItem(i);
                //如果不是多选模式，一轮循环则直接跳出，即如果有多个内容相同的item则默认选择第一个
                if (responseMode != 2)break;
            }
        }
    }

    //设置选中项,当有GONE和VISIBLE的操作时此方法更加安全
    public void setSelectItem(final int position, boolean delay){
        if(delay){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    setSelectItem(position);
                }
            });
        }else {
            setSelectItem(position);
        }
    }

    //设置选中项,item:item的内容,当有GONE和VISIBLE的操作时此方法更加安全
    public void setSelectItem(final String item, boolean delay){
        if(delay){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    setSelectItem(item);
                }
            });
        }else {
            setSelectItem(item);
        }
    }

    //清除选中项，如果当前View有GONE和VISIBLE的操作，请使用具有二参的重载函数
    public void clearSelectItem(int position){
        TextView textView;
        //清除选中的背景、颜色
        textView = (TextView) getChildAt(position);
        if (textView != null){
            //设置背景
            if (defaultDrawable != -1){
                textView.setBackgroundResource(defaultDrawable);
            }
            if(defaultColor != forbiddenColor){
                textView.setBackgroundColor(defaultColor);
            }
            //设置字体颜色
            if (defaultTextColor != forbiddenColor){
                textView.setTextColor(color(defaultTextColor));
            }
        }
        choosePosition.remove(position);
    }

    //设置选中项,item:item的内容
    public void clearSelectItem(String item){
        for (int i=0; i<children.size(); i++){
            if (children.get(i).equals(item)){
                clearSelectItem(i);
            }
        }
    }

    //设置选中项,当有GONE和VISIBLE的操作时此方法更加安全
    public void clearSelectItem(final int position, boolean delay){
        if(delay){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    clearSelectItem(position);
                }
            });
        }else {
            clearSelectItem(position);
        }
    }

    //设置选中项,item:item的内容,当有GONE和VISIBLE的操作时此方法更加安全
    public void clearSelectItem(final String item, boolean delay){
        if(delay){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    clearSelectItem(item);
                }
            });
        }else {
            clearSelectItem(item);
        }
    }

    //清除所有选中状态
    public void clearAll(){
        for (int i=0; i<children.size(); i++){
            clearSelectItem(i);
        }
    }

    //移除所有view，注意他和clearAll()的区别
    //clearAll()仅仅是清除状态，并没有移除里面的view
    public void removeAll(){
        this.children.clear();
        this.choosePosition.clear();
        requestLayout();
    }


    //重写刷新方法，选中的位置也要清除
    @Override
    public void refresh(List<String> children) {
        if (children == null)return;
        this.children.clear();
        this.children.addAll(children);
        this.choosePosition.clear();
        requestLayout();
    }

    //设置响应模式
    public void setResponseMode(int responseMode) {
        this.responseMode = responseMode;
    }

    public SparseArray<Boolean> getChoosePosition() {
        return choosePosition;
    }

    private int color(int color){
        return color;
    }

    public interface SelectChangeListener{
        public void onSelect(int position, String content);
        public void unSelect(int position, String content);
    }

    public void setSelectChangeListener(SelectChangeListener selectChangeListener) {
        this.selectChangeListener = selectChangeListener;
    }
}
