package cn.denua.v2ex.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.ViewGroup;

import cn.denua.v2ex.Config;
import cn.denua.v2ex.ConfigRefEnum;
import cn.denua.v2ex.R;

import static cn.denua.v2ex.utils.StatusBarUtil.getStatusBarHeight;

/*
 * base activity
 *
 * @user denua
 * @date 2018/10/20
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        UnexpectedExceptionHandler.getInstance().init();

        setTheme();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        ImmersionBar.with(this)
//                .navigationBarAlpha(0.2f)
//                .init();

//        ViewGroup viewGroup = (ViewGroup ) getWindow().getDecorView().findViewById(android.R.id.content);
//        BarUtils.addMarginTopEqualStatusBarHeight(viewGroup);

//        addStatusBarPlaceHolder();
    }

    protected void initView(){
        
    }

    protected void setSwipeRefreshTheme(SwipeRefreshLayout swipeRefreshLayout){

        TypedValue colorAccent = new TypedValue();
        TypedValue colorPrimaryDark = new TypedValue();
        getTheme().resolveAttribute(R.attr.attr_color_accent,colorAccent,
                true);
        getTheme().resolveAttribute(R.attr.attr_color_primary_dark,
                colorPrimaryDark, true);
        swipeRefreshLayout.setColorSchemeResources(colorAccent.resourceId,
                colorPrimaryDark.resourceId);
    }
    protected void setTheme(){
        int theme = Config.ThemeEnum.getThemeResByName(Config.getConfig(ConfigRefEnum.CONFIG_THEME));
        setTheme(theme);
    }

    protected int getColorAttr(int attr){

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    protected int getColorAccent(){

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.attr_color_accent, typedValue, true);
        return typedValue.data;
    }

    protected void setThemeNoActionBar(){
        switch (Config.ThemeEnum.getThemeResByName(Config.getConfig(ConfigRefEnum.CONFIG_THEME))){
            case R.style.MainTheme:
                setTheme(R.style.MainTheme_NoActionbar);
                break;
            case R.style.GreenTheme:
                setTheme(R.style.GreenTheme_NoActionbar);
                break;
            case R.style.TealTheme:
                setTheme(R.style.TealTheme_NoActionbar);
                break;
            case R.style.IndigoTheme:
                setTheme(R.style.IndigoTheme_NoActionbar);
                break;
            case R.style.OrangeTheme:
                setTheme(R.style.OrangeTheme_NoActionbar);
                break;
            default:
                break;
        }
    }

    protected void addStatusBarPlaceHolder(){

        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.setPadding(0, getStatusBarHeight(this), 0, 0);
    }
}
