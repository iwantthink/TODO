# 1. 监听

添加监听的位置在:MessageProcessor->monitorViewTreeChange


1. OnGlobalLayoutListener

2. OnScrollChangedListener

3. OnGlobalFocusChangeListener

Method:

    public void saveNewWindowImpressionDelayed() {
        ThreadUtils.postOnUiThreadDelayed(new Runnable() {
            public void run() {
                MessageProcessor.this.saveAllWindowImpress(true);
            }
        }, 500L);
    }


    private Runnable mSaveAllWindowImpression = new Runnable() {
        public void run() {
            MessageProcessor.this.flushPendingPageEvent();
            MessageProcessor.this.saveAllWindowImpress();
        }
    };