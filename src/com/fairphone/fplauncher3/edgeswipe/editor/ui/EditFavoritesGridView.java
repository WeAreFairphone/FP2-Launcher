/*
 * Copyright (C) 2013 Fairphone Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fairphone.fplauncher3.edgeswipe.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

public class EditFavoritesGridView extends HeaderGridView
{
    public interface OnEditFavouritesIconDraggedListener
    {
        public void OnEditFavouritesIconDragged(AdapterView<?> parent, View view, int position, long id);
        public void OnEditFavouritesIconDragEnded();
    }
    
    
    private float touchStartX = 0;
    private float touchStartY = 0;
    private int selectedChild = INVALID_POSITION;
    private boolean hasStartedDraggingOut = false;
    private boolean ignoreDragging = false;
    private OnEditFavouritesIconDraggedListener listener=null;
    private final float xBias = 2.0f;
    private final float minMoveDistance = 40;
    
    
    public EditFavoritesGridView(Context context)
    {
        super(context);
        init();
    }

	public EditFavoritesGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public EditFavoritesGridView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }
    

    private void init() {
		setIsInFront(false);
	}
    
    
    public void setOnEditFavouritesIconDraggedListener(OnEditFavouritesIconDraggedListener listener) 
    {
        this.listener = listener;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        switch(ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                touchStartX = ev.getX();
                touchStartY = ev.getY();
                selectedChild = pointToPosition((int)touchStartX, (int)touchStartY);
                ignoreDragging = (selectedChild==INVALID_POSITION);
                hasStartedDraggingOut = false;
            }
            break;
            
            case MotionEvent.ACTION_MOVE:
            {
                if(!ignoreDragging && !hasStartedDraggingOut)
                {
                    float xDif = touchStartX - ev.getX();
                    float yDif = ev.getY()-touchStartY;
                    
                    float absYDif = Math.abs(yDif);
                    float absXDif = Math.abs(xDif)*xBias;
                    
                    
                    if(absXDif>absYDif && xDif>minMoveDistance)//are we dragging mostly to the right?
                    {
                        hasStartedDraggingOut = true;
                        MotionEvent cancelEvent = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(), ev.getMetaState());
                        super.onTouchEvent(cancelEvent);
                        if(listener!=null)
                        {
                            // Subtract the number of columns in order to get the correct item
                            // due to the header
                        	int numColumns = getNumColumns();
                            View childView = getChildAt(selectedChild-(getFirstVisiblePosition() + numColumns));
                            if(childView!=null)
                            {
                            	int selectedItem = selectedChild - numColumns;
                                listener.OnEditFavouritesIconDragged(this, childView, selectedItem, getAdapter().getItemId(selectedItem));
                            }
                        }
                    }
                    else if(absXDif<absYDif && absYDif>minMoveDistance)
                    {
                        ignoreDragging = true;
                    }
                }
            }
            break;
            
            case MotionEvent.ACTION_UP:
            {
                if(listener!=null)
                {
                    listener.OnEditFavouritesIconDragEnded();
                }
            }
            break;

        }
        
        if(!hasStartedDraggingOut)
        {
            return super.onTouchEvent(ev);
        }
        else
        {
            return true;
        }
    }
}
