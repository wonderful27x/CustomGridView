package com.example.customgridview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

/**
 *  @Author wonderful
 *  @Date 2020-9-3
 *  @Version 1.0
 *  @Description 自定义gridView,增加tab标签
 */
public final class TabContentGridView extends WonderfulGridView{

    //tab标签的背景Drawable
    private int tabSelectDrawable;
    private int tabDefaultDrawable;
    //分割线颜色
    private int selectDividerColor;
    private int defaultDividerColor;

    //tab标签
    private List<String> tabs = new ArrayList<>();

    public TabContentGridView(Context context) {
        this(context,null);
    }

    public TabContentGridView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TabContentGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    //初始化
    @SuppressLint("ResourceAsColor")
    private void init(Context context, AttributeSet attrs){

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.wonderfulGridViewStyle);

        tabSelectDrawable = typedArray.getResourceId(R.styleable.wonderfulGridViewStyle_tabSelectDrawable,R.drawable.rect_blue_blank);
        tabDefaultDrawable = typedArray.getResourceId(R.styleable.wonderfulGridViewStyle_tabDefaultDrawable,R.drawable.rect_gray_blank);

        selectDividerColor = typedArray.getColor(R.styleable.wonderfulGridViewStyle_selectDividerColor,forbiddenColor);
        defaultDividerColor = typedArray.getColor(R.styleable.wonderfulGridViewStyle_defaultDividerColor,forbiddenColor);

        //如果没有设置分割线的颜色则默认和字体颜色一致
        if (selectDividerColor == forbiddenColor){
            selectDividerColor = selectTextColor;
        }
        if (defaultDividerColor == forbiddenColor){
            defaultDividerColor = defaultTextColor;
        }

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
            final String tab = tabs.get(i);

            TabContentView tabContentView = new TabContentView(context,this);
            if (maxLine >0){
                tabContentView.content.setMaxLines(maxLine);
            }
            tabContentView.targetView.setLayoutParams(new ViewGroup.LayoutParams(width, height));

            //设置默认背景/字体颜色
            //设置背景drawable,
            if (defaultDrawable != -1){
                tabContentView.content.setBackgroundResource(defaultDrawable);
            }
            if (tabDefaultDrawable != -1){
                tabContentView.tab.setBackgroundResource(tabDefaultDrawable);
            }
            //设置背景颜色，优先级更高，如果都设置了则会覆盖drawable
            if (defaultColor != forbiddenColor){
                tabContentView.content.setBackgroundColor(defaultColor);
                tabContentView.tab.setBackgroundColor(defaultColor);
            }
            //设置字体颜色
            if (defaultTextColor != forbiddenColor){
                tabContentView.content.setTextColor(defaultTextColor);
                tabContentView.tab.setTextColor(defaultTextColor);
            }
            //设置分割线颜色
            if (defaultDividerColor != forbiddenColor){
                tabContentView.divider.setBackgroundColor(defaultDividerColor);
            }

            //如果是点击模式，直接设置选择器即可
            //TODO 有bug，不起作用
            if (responseMode == 0){
                //TODO 点击模式下只能修改targetView的样式，因为他内部还有子view，无法统一处理
                //设置背景选择器
                StateListDrawable drawable = new StateListDrawable();
                Drawable drawableSelect = ContextCompat.getDrawable(context,selectDrawable);
                Drawable drawableNormal = ContextCompat.getDrawable(context,defaultDrawable);
                drawable.addState(new int[]{android.R.attr.state_pressed},drawableSelect);//选中
                drawable.addState(new int[]{},drawableNormal);                            //未选中
                //targetView选择器
                tabContentView.targetView.setBackground(drawable);
            }
            //否则就是单选或多选模式，需要设置选中的颜色
            else {
                //设置当前选中的背景、颜色
                //这很重要，因为当一个View由GONE变为VISIBLE的时候会触发onMeasure方法
                //而onMeasure会多次调用addChildrenView，这意味着childView会被重新创建
                //如果choosePosition.get(i)==true说明之前就已经做过选择操作，因此需要设置当前childView为选中状态
                if (choosePosition.get(i) != null && choosePosition.get(i)){
                    //设置背景
                    if (selectDrawable != -1){
                        tabContentView.content.setBackgroundResource(selectDrawable);
                    }
                    if (tabSelectDrawable != -1){
                        tabContentView.tab.setBackgroundResource(tabSelectDrawable);
                    }
                    if(selectColor != forbiddenColor){
                        tabContentView.content.setBackgroundColor(selectColor);
                        tabContentView.tab.setBackgroundColor(selectColor);
                    }
                    //设置字体颜色
                    if (selectTextColor != forbiddenColor){
                        tabContentView.content.setTextColor(selectTextColor);
                        tabContentView.tab.setTextColor(selectTextColor);
                    }
                    //设置分割线颜色
                    if (selectDividerColor != forbiddenColor){
                        tabContentView.divider.setBackgroundColor(selectDividerColor);
                    }
                }
            }
            //设置padding
            if (gridPadding != -1){
                tabContentView.content.setPadding(gridPadding,gridPadding,gridPadding,gridPadding);
                tabContentView.tab.setPadding(gridPadding,gridPadding,gridPadding,gridPadding);
            }else {
                tabContentView.content.setPadding(leftPadding,topPadding,rightPadding,bottomPadding);
                tabContentView.tab.setPadding(leftPadding,topPadding,rightPadding,bottomPadding);
            }
            //居中显示
            tabContentView.content.setGravity(Gravity.CENTER);
            tabContentView.tab.setGravity(Gravity.CENTER);
            //设置字体大小
            if (textSize != -1){
                tabContentView.content.setTextSize(textSize);
                tabContentView.tab.setTextSize(textSize);
            }
            //设置字体内容
            tabContentView.content.setText(child);
            tabContentView.tab.setText(tab);
            //设置点击监听事件
            final int finalI = i;
            tabContentView.targetView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(finalI,child);
                }
            });

            addView(tabContentView.targetView);
        }
    }

    @Override
    public void clearSelectItem(int position) {
        //清除选中的背景、颜色
        TabContentView tabContentView = new TabContentView(getChildAt(position));
        if (tabContentView.targetView != null){
            //设置背景
            if (defaultDrawable != -1){
                tabContentView.content.setBackgroundResource(defaultDrawable);
            }
            if (tabDefaultDrawable != -1){
                tabContentView.tab.setBackgroundResource(tabDefaultDrawable);
            }
            if(defaultColor != forbiddenColor){
                tabContentView.content.setBackgroundColor(defaultColor);
                tabContentView.tab.setBackgroundColor(defaultColor);
            }
            //设置字体颜色
            if (defaultTextColor != forbiddenColor){
                tabContentView.content.setTextColor(defaultTextColor);
                tabContentView.tab.setTextColor(defaultTextColor);
            }
            //设置分割线颜色
            if (defaultDividerColor != forbiddenColor){
                tabContentView.divider.setBackgroundColor(defaultDividerColor);
            }
        }
        choosePosition.remove(position);
    }

    @Override
    public void setSelectItem(int position) {
        //点击模式下将不会有任何作用
        if (responseMode == 0){
            //TODO
        }
        //单选模式
        else if (responseMode == 1){
            //先清除所有选中的背景、颜色
            clearAll();
            //设置当前选中的背景、颜色
            TabContentView tabContentView = new TabContentView(getChildAt(position));
            if (tabContentView.targetView != null){
                //设置背景
                if (selectDrawable != -1){
                    tabContentView.content.setBackgroundResource(selectDrawable);
                }
                if (tabSelectDrawable != -1){
                    tabContentView.tab.setBackgroundResource(tabSelectDrawable);
                }
                if(selectColor != forbiddenColor){
                    tabContentView.content.setBackgroundColor(selectColor);
                    tabContentView.tab.setBackgroundColor(selectColor);
                }
                //设置字体颜色
                if (selectTextColor != forbiddenColor){
                    tabContentView.content.setTextColor(selectTextColor);
                    tabContentView.tab.setTextColor(selectTextColor);
                }
                //设置分割线颜色
                if (selectDividerColor != forbiddenColor){
                    tabContentView.divider.setBackgroundColor(selectDividerColor);
                }
            }
        }
        //多选模式
        else if (responseMode == 2){
            //设置当前选中的背景、颜色
            TabContentView tabContentView = new TabContentView(getChildAt(position));
            if (tabContentView.targetView != null){
                //设置背景
                if (selectDrawable != -1){
                    tabContentView.content.setBackgroundResource(selectDrawable);
                }
                if (tabSelectDrawable != -1){
                    tabContentView.tab.setBackgroundResource(tabSelectDrawable);
                }
                if(selectColor != forbiddenColor){
                    tabContentView.content.setBackgroundColor(selectColor);
                    tabContentView.tab.setBackgroundColor(selectColor);
                }
                //设置字体颜色
                if (selectTextColor != forbiddenColor){
                    tabContentView.content.setTextColor(selectTextColor);
                    tabContentView.tab.setTextColor(selectTextColor);
                }
                //设置分割线颜色
                if (selectDividerColor != forbiddenColor){
                    tabContentView.divider.setBackgroundColor(selectDividerColor);
                }
            }
        }
        //保存选中的位置
        choosePosition.put(position,true);
    }

    @Override
    public void removeAll() {
        this.children.clear();
        this.tabs.clear();
        this.choosePosition.clear();
        requestLayout();
    }

    @Override
    public void addChildrenView(List<String> children) {
        throw new RuntimeException("请使用二参的addChildrenView重载方法！！！");
    }

    public void addChildrenView(List<String> children, List<String> tabs) {
        if (children == null || tabs == null)return;
        if (children.size() != tabs.size()){
            throw new IllegalArgumentException("children和tabs的数量必须一致！！！");
        }
        this.children.addAll(children);
        this.tabs.addAll(tabs);
    }

    //禁止使用此方法
    @Override
    public void refresh(List<String> children) {
        throw new RuntimeException("请使用二参的refresh重载方法！！！");
    }

    public void refresh(List<String> children, List<String> tabs) {
        if (children == null || tabs == null)return;
        if (children.size() != tabs.size()){
            throw new IllegalArgumentException("children和tabs的数量必须一致！！！");
        }
        this.children.clear();
        this.children.addAll(children);
        this.tabs.clear();
        this.tabs.addAll(tabs);
        this.choosePosition.clear();
        requestLayout();
    }

    private static class TabContentView{

        private View targetView;            //目标view
        private TextView tab;               //tab标签
        private TextView content;           //内容
        private View divider;               //分割线

        TabContentView(Context context, ViewGroup groupView){
            LayoutInflater inflater = LayoutInflater.from(context);
            targetView = inflater.inflate(R.layout.tab_content_layout,groupView,false);
            tab = targetView.findViewById(R.id.tab);
            content = targetView.findViewById(R.id.content);
            divider = targetView.findViewById(R.id.divider);
        }

        TabContentView(View targetView){
            if (targetView == null)return;
            this.targetView = targetView;
            tab = targetView.findViewById(R.id.tab);
            content = targetView.findViewById(R.id.content);
            divider = targetView.findViewById(R.id.divider);
        }
    }
}
