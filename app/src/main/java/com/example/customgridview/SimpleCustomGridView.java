package com.example.customgridview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import static android.content.Context.WINDOW_SERVICE;

/**
 *  @Author wonderful
 *  @Date 2020-8-11
 *  @Version 1.0
 *  @Description 自定义gridView,主要用于排列规整的场景
 */
public class SimpleCustomGridView extends ViewGroup {

    private static final String TAG = "SimpleCustomGridView";

    private int column;              //列数，默认一列
    private int gapVertical;         //每列中间的间隔
    private int gapHorizontal;       //每行中间的间隔

    //子view的padding
    protected int leftPadding;
    protected int rightPadding;
    protected int topPadding;
    protected int bottomPadding;
    protected int gridPadding;

    //背景Drawable
    protected int selectDrawable;
    protected int defaultDrawable;

    //颜色选择器
    protected int colorSelector;

    //字体大小
    protected int textSize;

    protected Context context;
    protected List<String> children = new ArrayList<>();//数据源

    private ItemClickListener itemClickListener;

    public SimpleCustomGridView(Context context) {
        this(context, null);
    }

    public SimpleCustomGridView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SimpleCustomGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    //初始化
    @SuppressLint("ResourceAsColor")
    private void init(Context context, AttributeSet attrs){
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.wonderfulGridViewStyle);

        column = typedArray.getInteger(R.styleable.wonderfulGridViewStyle_column,1);
        gapVertical = typedArray.getDimensionPixelSize(R.styleable.wonderfulGridViewStyle_gapVertical,0);
        gapHorizontal = typedArray.getDimensionPixelSize(R.styleable.wonderfulGridViewStyle_gapHorizontal,0);

        leftPadding = typedArray.getDimensionPixelSize(R.styleable.wonderfulGridViewStyle_leftPadding,0);
        rightPadding = typedArray.getDimensionPixelSize(R.styleable.wonderfulGridViewStyle_rightPadding,0);
        topPadding = typedArray.getDimensionPixelSize(R.styleable.wonderfulGridViewStyle_topPadding,0);
        bottomPadding = typedArray.getDimensionPixelSize(R.styleable.wonderfulGridViewStyle_bottomPadding,0);
        gridPadding = typedArray.getDimensionPixelSize(R.styleable.wonderfulGridViewStyle_gridPadding,-1);

        selectDrawable = typedArray.getResourceId(R.styleable.wonderfulGridViewStyle_selectDrawable,R.drawable.rect_blue_blank);
        defaultDrawable = typedArray.getResourceId(R.styleable.wonderfulGridViewStyle_defaultDrawable,R.drawable.rect_gray_blank);

        colorSelector = typedArray.getResourceId(R.styleable.wonderfulGridViewStyle_colorSelector,R.drawable.color_selector);

        textSize = typedArray.getDimensionPixelSize(R.styleable.wonderfulGridViewStyle_gridTextSize,-1);

        typedArray.recycle();
    }


    /**
     * 分两种情况
     * 一：使用match_parent或指定大小
     * 这种情况下子view的宽度是固定的，高度为包裹内容是的高度（可能是多行），
     * 但是gridView的所有子view的宽高必须一致，因此选择最大子view高度作为最终高度
     * 测量策略是
     * 1.得到子view宽度：由总宽度和列数及padding计算得到
     * 2.得到子view高度：以1的宽度和WRAP_CONTENT作为高度测量子view，并选出所有子view的最大高度
     * 3.以1、2得到的宽高作为每个子view的宽高，重新设置，并测量
     * 二：使用wrap_content
     * 这种情况下子view的宽度和高度都是固定的，宽度等于最大子view的宽度（只有一行），高度等于子view的高度
     * 测量策略是
     * 1.得到子view的高度，在MaxLines=1的约束下测量得到
     * 2.得到子view的宽度，以1的高度，WRAP_CONTENT作为宽测量子view，并选出最大子view的宽度，再次约束宽度不能超过屏幕宽度下指定列数下每一列的宽度
     * 3.以1、2得到的宽高作为每个子view的宽高，重新设置，并测量
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT){
            measureAT_MOST(widthMeasureSpec,heightMeasureSpec);
        }else {
            measureEXACTLY(widthMeasureSpec,heightMeasureSpec);
        }
    }

    //宽为match_parent或指定大小情况下测量策略
    private void measureEXACTLY(int widthMeasureSpec, int heightMeasureSpec){
        //先设置一次宽高以便获取测量宽高
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width,height);
        //根据测量宽度计算每列的宽度
        int columnWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - (column - 1) * gapVertical) / column;

        //重新添加子view,并宽度设置为没一列的宽度，高度设置为WRAP_CONTENT
        //这样做是为了下面获取宽度为每列的宽度columnWidth，高度为WRAP_CONTENT约束下的测量高度
        addChildrenView(columnWidth, ViewGroup.LayoutParams.WRAP_CONTENT,-1);
        //在以上约束下测量一次子view
        measureChildren(widthMeasureSpec,heightMeasureSpec);

        //获取最大子控件高度
        int maxHeight = getMaxMeasureSize(1);
        //以columnWidth和maxHeight为最终宽高，重新添加子view
        addChildrenView(columnWidth,maxHeight,-1);
        //再次测量
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        //设置自己宽高
        width = View.MeasureSpec.getSize(widthMeasureSpec);
        height = maxHeight * getRows() + (getRows() - 1) * gapHorizontal + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width,height);
    }

    //宽为wrap_content情况下测量策略
    private void measureAT_MOST(int widthMeasureSpec, int heightMeasureSpec){
        //计算最大宽度
        int maxWith = (getWindowWidth() - getPaddingLeft() - getPaddingRight()) / column;
        //以最大高度为1行测量
        addChildrenView(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        //获取最大子view宽度
        int maxMeasureWidth = getMaxMeasureSize(0);
        int width = Math.min(maxWith,maxMeasureWidth);
        //获取最大子view高度
        int height = getMaxMeasureSize(1);
        //重新添加并测量
        addChildrenView(width,height,1);
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        //设置自己宽高
        //如果子view的数量小于列数，则宽按照子view的数量计算宽度
        if (getChildCount() < column){
            width = width * getChildCount() + (getChildCount() - 1) * gapVertical + getPaddingLeft() + getPaddingRight();
        }
        //否则按照列数计算宽度
        else{
            width = width * column + (column - 1) * gapVertical + getPaddingLeft() + getPaddingRight();
        }
        height = height * getRows() + (getRows() - 1) * gapHorizontal + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width,height);
        //如果在WRAP_CONTENT的情况下没有任何子元素则宽直接为0
        if (getChildCount() == 0){
            setMeasuredDimension(0,height);
        }
    }

    @Override
    protected void onLayout(boolean b, int i0, int i1, int i2, int i3) {
        //计算行数
        int rows = getRows();
        //一行一行地摆放
        for (int i=0; i<rows; i++){
            if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT){
                layoutRowAT_MOST(i);
            }else {
                layoutRowEXACTLY(i);
            }
        }
    }

    //计算行数
    private int getRows(){
        int count = getChildCount();
        Log.d(TAG, "ChildCount: " + count);
        int rows;
        if(count % column ==0){
            rows = count / column;
        }else {
            rows = count / column + 1;
        }
        Log.d(TAG, "Rows: " + rows);
        return rows;
    }

    ///摆放一行,宽为match_parent或指定大小情况下摆放策略
    private void layoutRowEXACTLY(int rows){
        int top = rows * getChildAt(0).getMeasuredHeight() + rows * gapHorizontal + getPaddingTop();
        int left = getPaddingLeft();

        int startIndex = rows * column;
        //此情况下子view的宽等于每一列的宽
        int width = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - (column - 1) * gapVertical) / column;

        for (int i=0; i<column; i++){
            //获取子控件
            int childIndex = startIndex + i;
            if (childIndex >= getChildCount())return;
            View child = getChildAt(childIndex);
            //摆放
            int right = left + width;
            int bottom = top + child.getMeasuredHeight();
            child.layout(left,top,right,bottom);
            //left 偏移
            left += width + gapVertical;

        }
    }

    //摆放一行,宽为wrap_content情况下摆放策略
    private void layoutRowAT_MOST(int rows){
        int top = rows * getChildAt(0).getMeasuredHeight() + rows * gapHorizontal + getPaddingTop();
        int left = getPaddingLeft();

        int startIndex = rows * column;
        //此情况下子view的宽就等于自己的宽，这在测量过程中已经处理了
        int width = getChildAt(0).getMeasuredWidth();

        for (int i=0; i<column; i++){
            //获取子控件
            int childIndex = startIndex + i;
            if (childIndex >= getChildCount())return;
            View child = getChildAt(childIndex);
            //摆放
            int right = left + width;
            int bottom = top + child.getMeasuredHeight();
            child.layout(left,top,right,bottom);
            //left 偏移
            left += width + gapVertical;
        }
    }

    /**
     * 获取子元素的最大测量宽高
     * @param type 0：宽 ~0：高
     * @return
     */
    private int getMaxMeasureSize(int type){
        int count = getChildCount();
        int maxSize = 0;
        for (int i=0; i<count; i++){
            int measureSize;
            if (type == 0){
                measureSize = getChildAt(i).getMeasuredWidth();
            }else {
                measureSize = getChildAt(i).getMeasuredHeight();
            }
            maxSize = maxSize < measureSize ? measureSize : maxSize;
        }
        return maxSize;
    }

    /**
     * 添加子view
     * @param width 子view的宽
     * @param height 子view的高
     */
    protected void addChildrenView(int width,int height,int maxLine){
        removeAllViews();
        for (int i=0; i<children.size(); i++){
            final String child = children.get(i);

            TextView textView = new TextView(context);
            if (maxLine >0){
                textView.setMaxLines(maxLine);
            }
            textView.setLayoutParams(new ViewGroup.LayoutParams(width, height));

            //设置背景选择器
            StateListDrawable drawable = new StateListDrawable();
            Drawable drawableSelect = ContextCompat.getDrawable(context,selectDrawable);
            Drawable drawableNormal = ContextCompat.getDrawable(context,defaultDrawable);
            //选中
            drawable.addState(new int[]{android.R.attr.state_pressed},drawableSelect);
            //未选中
            drawable.addState(new int[]{},drawableNormal);
            textView.setBackground(drawable);
            //设置字体颜色选择器
            textView.setTextColor(ContextCompat.getColorStateList(context, colorSelector));

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

    //处理点击事件
    protected void onItemClick(int position, String content){
        if (itemClickListener != null){
            itemClickListener.onItemClick(position,content);
        }
    }

    //添加子view
    public void addChildrenView(List<String> children){
        if (children == null)return;
        this.children.addAll(children);
    }

    //刷新数据
    public void refresh(){
        requestLayout();
    }

    //刷新数据
    public void refresh(List<String> children){
        if (children == null)return;
        this.children.clear();
        this.children.addAll(children);
        requestLayout();
    }

    //获得屏幕宽
    private int getWindowWidth(){
        //获取windowManager
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        //获取屏幕对象
        Display defaultDisplay = windowManager.getDefaultDisplay();
        //获取屏幕的宽、高，单位是像素
        return defaultDisplay.getWidth();
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        public void onItemClick(int position, String content);
    }
}
