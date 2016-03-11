# Buff

The buff can reduce your development time, has the following features (support api >= 8):
 > - pull down to refresh
 > - pull up to load more
 > -  show loading view
 > -  show retry view (show it when requesting data failed and setOnRetryClickListener)
 > -  show empty view (when no data)
    
    
### show views

   * loading view and retry view
   
   <img src="https://raw.githubusercontent.com/nukc/buff/master/art/showViews.gif">
    
### refresh / load more 

   * Mode: outside (default)
    
   <img src="https://raw.githubusercontent.com/nukc/buff/master/art/outside.gif">
     
   * Mode: inside 
   the refresh is [Yalantis/Phoenix](https://github.com/Yalantis/Phoenix) style 
   
   if you want to use the refresh/loadmore style, you can see the sample project
    
   <img src="https://raw.githubusercontent.com/nukc/buff/master/art/inside.gif">
    
## Usage

   add the dependency to your build.gradle:
```
   dependencies {
        compile 'com.github.nukc.buff:library:1.0'
   }
```
   
   add the PageLayout weight
   
```xml
   <com.github.nukc.buff.PageLayout 
       android:id="@+id/pageLayout"
       android:layout_width="match_parent"
       android:layout_height="match_parent">
   
       <android.support.v7.widget.RecyclerView
           android:id="@+id/recyclerView"
           android:layout_width="match_parent"
           android:layout_height="match_parent" />
   </com.github.nukc.buff.PageLayout>
  
```
   
   set in java code 
   
```java
   
   //only pull down to refresh
   mPageLayout.setOnRefreshListener(new PageLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
                   
        }
   });
   
   
   //if you add app:loadRetryEnabled="true" in layout.xml
   mPageLayout.setOnRefreshAndLoadMoreListener(new PageLayout.OnRefreshAndLoadMoreListener() {
        @Override
        public void onRefresh() {

        }
   
        @Override
        public void onLoadMore() {
   
        }
   });
   
   //refresh complete
   mPageLayout.setRefreshing(false);
   
   //load more complete
   mPageLayout.setLoadingMore(false);
  
```
  
   set retry view click listener
   
```java
   mPageLayout.setOnRetryClickListener(new LoadRetryLayout.OnRetryClickListener() {
        @Override
        public void onRetryClick(View v) {
                   
        }
   });
   
   //Called when requesting data successfully
   mPageLayout.onRequestSuccess();
   
```
   
   

## Customization

   you can implements IPullUIHandler to customize refresh/loadmore style:
   
```java
   public interface IPullUIHandler {
       void onPulling(float scrollTop, int targetY, int totalDragDistance);
   
       void onRefresh(int totalDragDistance);
   
       void onStop(float dragPercent);
   }
   
```
   
   can set own view
   
```java
   mPageLayout.getLoadRetryLayout().setLoadingView( View/LayoutRes );
   //setRetryView() setEmptyView()
   
```
   
## Custom Attribute

```xml
    <resources>
        <declare-styleable name="PageLayout">
            <attr name="loadRetryEnabled" format="boolean" />
            <attr name="loadMoreEnabled" format="boolean" />
            <attr name="layoutMode" format="enum">
                <enum name="inside" value="0" />
                <enum name="outside" value="1" />
            </attr>
        </declare-styleable>
    </resources>
```

## Thanks
  * [SwipeRefreshLayout](https://developer.android.com/reference/android/support/v4/widget/SwipeRefreshLayout.html)
  * [Yalantis/Phoenix](https://github.com/Yalantis/Phoenix)
   
## License

    The MIT License (MIT)
    
    Copyright (c) 2016 Nukc
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
