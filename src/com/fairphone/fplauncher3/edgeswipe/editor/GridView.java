package com.fairphone.fplauncher3.edgeswipe.editor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ListView.FixedViewInfo;

/**
 * Componente de GridView con posibilidad de añadir un header mediante
 * addHeaderView(View v, Object data, boolean isSelectable); addHeaderView(View
 * v); No funciona con ArrayAdapter sino con adapters extendidos de BaseAdapter
 * 
 * @author munix
 * 
 */
public class GridView extends android.widget.GridView implements OnScrollListener, android.widget.AdapterView.OnItemClickListener
{
    private int mScrollOfsset;
    private int initialTopPadding = 0;
    private int mDisplayWidth = 0;
    private int headerViewHeight = 0;
    private ListAdapter originalAdapter;
    private BaseAdapter fakeAdapter;
    private int lastPos = 0;
    private OnScrollListener scrollListenerFromActivity;
    private OnItemClickListener clickListenerFromActivity;
    private FixedViewInfo mHeaderViewInfo;
    private Boolean setFixed = false;
    private Boolean bringToFront = true;
    private int verticalSpacing = 0;

    public GridView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    public GridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public GridView(Context context)
    {
        super(context);
        init(context);
    }

    private void init(Context context)
    {
        super.setOnScrollListener(this);
    }

    @Override
    public void onDetachedFromWindow()
    {
        if (originalAdapter != null)
        {
            originalAdapter.unregisterDataSetObserver(originalAdapterDataSetObserver);
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void setVerticalSpacing(int spacing)
    {
        this.verticalSpacing = spacing;
        super.setVerticalSpacing(spacing);
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener l)
    {
        clickListenerFromActivity = l;
        super.setOnItemClickListener(this);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l)
    {
        scrollListenerFromActivity = l;
        //Guardo la referencia del scroll para poder usar ambos
        super.setOnScrollListener(this);
    }

    @Override
    public void setAdapter(ListAdapter a)
    {
        originalAdapter = a;
        fakeAdapter = new mListAdapter();
        //Registramos un DataSetObserver para que cuando se produzcan cambios en el adaptador original lo podamos reflejar
        //en el fakeAdapter
        originalAdapter.registerDataSetObserver(originalAdapterDataSetObserver);
        super.setAdapter(fakeAdapter);
    }

    DataSetObserver originalAdapterDataSetObserver = new DataSetObserver()
    {
        @Override
        public void onChanged()
        {
            fakeAdapter.notifyDataSetChanged();
        }

        @Override
        public void onInvalidated()
        {
            fakeAdapter.notifyDataSetInvalidated();
        }
    };

    /**
     * Adaptador que recubre el original para poder dar espacio a la cabecera
     * 
     * @author munix
     * 
     */
    public class mListAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return originalAdapter.getCount() > 0 ? originalAdapter.getCount() + GridView.this.getNumColumnsCompat() : 0;
        }

        @Override
        public Object getItem(int position)
        {
            return originalAdapter.getItem(position + GridView.this.getNumColumnsCompat());
        }

        @Override
        public long getItemId(int position)
        {
            return originalAdapter.getItemId(position + GridView.this.getNumColumnsCompat());
        }

        class InternalViewHolder
        {
            View view;
        }

        @Override
        public int getViewTypeCount()
        {
            return 2;
        }

        @Override
        public int getItemViewType(int position)
        {
            if (position < GridView.this.getNumColumnsCompat())
            {
                return IGNORE_ITEM_VIEW_TYPE;
            }
            else
            {
                return 1;
            }
        }

        @Override
        public View getView(int position, View convert, ViewGroup parent)
        {
            if (position < GridView.this.getNumColumnsCompat())
            {
                if (convert == null)
                {
                    convert = new LinearLayout(getContext());
                    ViewGroup.LayoutParams mParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, headerViewHeight);
                    mParams.height = headerViewHeight;
                    convert.setLayoutParams(mParams);
                    convert.setVisibility(View.INVISIBLE);
                }
            }
            else
            {
                int realPosition = position - GridView.this.getNumColumnsCompat();
                convert = originalAdapter.getView(realPosition, convert, parent);
            }
            return convert;
        }
    };

    /**
     * Añade la vista al layout
     * 
     * @param v
     *            la vista
     * @param data
     *            extra data
     * @param isSelectable
     *            foo
     */
    public void addHeaderView(final View v, final Object data, final boolean isSelectable)
    {
        mHeaderViewInfo = new ListView(getContext()).new FixedViewInfo();
        mHeaderViewInfo.view = v;
        mHeaderViewInfo.data = data;
        mHeaderViewInfo.isSelectable = isSelectable;

        setupView(v);

        int topPadding = getPaddingTop();
        if (initialTopPadding == 0)
        {
            initialTopPadding = topPadding;
        }
        headerViewHeight = v.getMeasuredHeight();

        ViewGroup parent = (ViewGroup) getParent();
        parent.addView(v, 0);
        if (bringToFront)
        {
            v.bringToFront();
        }
    }

    public FixedViewInfo getHeaderView()
    {
        return mHeaderViewInfo;
    }

    private void setupView(View v)
    {
        boolean isLayedOut = !((v.getRight() == 0) && (v.getLeft() == 0) && (v.getTop() == 0) && (v.getBottom() == 0));

        if (v.getMeasuredHeight() != 0 && isLayedOut)
            return;

        if (mDisplayWidth == 0)
        {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            mDisplayWidth = displaymetrics.widthPixels;

        }
        v.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        v.measure(MeasureSpec.makeMeasureSpec(mDisplayWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        v.layout(0, getTotalHeaderHeight(), v.getMeasuredWidth(), getTotalHeaderHeight() + v.getMeasuredHeight());
    }

    /**
     * Añade la vista al layout
     * 
     * @param v
     */
    public void addHeaderView(View v)
    {
        this.addHeaderView(v, null, false);
    }

    /**
     * Nos indica si la cabecera se pinta por delante o por detrás del scroll de
     * la lista
     * 
     * @return boolean
     */
    public Boolean isInFront()
    {
        return bringToFront;
    }

    /**
     * Define si la cabecera se pinta por delante del scroll o si el scroll va
     * por encima y la cabecera está fija
     * 
     * @param Boolean
     *            isInFront
     */
    public void setIsInFront(Boolean isInFront)
    {
        bringToFront = isInFront;
    }

    /**
     * Permite cambiar entre una cabecera fija o scrollable
     * 
     * @param Boolean
     *            fixed
     */
    public void setFixedHeader(Boolean fixed)
    {
        this.setFixed = fixed;
    }

    private void drawHeaders()
    {
        if (mHeaderViewInfo != null)
        {
            int startPos = -mScrollOfsset;
            //Para evitar ciclos infinitos de onDraw / drawHeaders porque si en onDraw le pongo el topMargin efectúa
            //repintado, entonces llama a drawHeaders y así....
            if (lastPos != startPos && !setFixed && bringToFront)
            {
                if (mScrollOfsset <= headerViewHeight)
                {
                    FrameLayout.LayoutParams mParams = (android.widget.FrameLayout.LayoutParams) mHeaderViewInfo.view.getLayoutParams();
                    mParams.topMargin = startPos;
                    mHeaderViewInfo.view.setLayoutParams(mParams);
                    mHeaderViewInfo.view.setVisibility(View.VISIBLE);
                }
                else
                {
                    mHeaderViewInfo.view.setVisibility(View.GONE);
                }
            }
            lastPos = startPos;
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (fakeAdapter != null)
        {
            drawHeaders();
        }
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas)
    {
        super.dispatchDraw(canvas);

    }

    private int getTotalHeaderHeight()
    {
        return headerViewHeight;
    }

    private int getNumColumnsCompat()
    {
        if (Build.VERSION.SDK_INT >= 11)
        {
            return getNumColumnsCompat11();

        }
        else
        {
            int columns = 0;
            int children = getChildCount();
            if (children > 0)
            {
                int width = getChildAt(0).getMeasuredWidth();
                if (width > 0)
                {
                    columns = getWidth() / width;
                }
            }
            return columns > 0 ? columns : AUTO_FIT;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private int getNumColumnsCompat11()
    {
        return getNumColumns();
    }

    private int getVerticalSpacingCompat()
    {
        if (Build.VERSION.SDK_INT >= 16)
        {
            return getVerticalSpacingCompat16();
        }
        else
        {
            return this.verticalSpacing;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getVerticalSpacingCompat16()
    {
        return getVerticalSpacing();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (this.getAdapter() != null)
        {
            int count = this.getChildCount();
            int totalHeaderHeight = getTotalHeaderHeight();

            if (count > this.getNumColumnsCompat())
            {
                View child = this.getChildAt(this.getNumColumnsCompat());
                if (child != null)
                {
                    mScrollOfsset =
                            ((firstVisibleItem / this.getNumColumnsCompat()) * child.getMeasuredHeight()) + totalHeaderHeight - child.getTop()
                                    + this.getVerticalSpacingCompat();
                }
            }
        }
        if (scrollListenerFromActivity != null)
        {
            scrollListenerFromActivity.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

    }

    /**
     * Elimina la cabecera
     */
    public void removeHeaderView()
    {
        if (mHeaderViewInfo != null)
        {
            FrameLayout parent = (FrameLayout) this.getParent();
            parent.removeView(mHeaderViewInfo.view);
            super.setAdapter(originalAdapter);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if (scrollListenerFromActivity != null)
        {
            scrollListenerFromActivity.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
    {
        if (clickListenerFromActivity != null)
        {
            clickListenerFromActivity.onItemClick(adapter, view, position - GridView.this.getNumColumnsCompat(), id);
        }
    }
}