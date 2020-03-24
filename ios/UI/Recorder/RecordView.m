//
//  RecordView.m
//  RNFFmpegRecorder
//
//  Created by HungDV on 3/23/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "RecordView.h"

#define marginDefault 24
#define sizeIconTop 30
#define recordBtnSize 75
#define heightProgressBar = 5

#define arrayIcon @[@"close", @"switch", @"flash_off"]
#define Max(a,b) ( ((a) > (b)) ? (a) : (b) )
#define Min(a,b) ( ((a) < (b)) ? (a) : (b) )

#define recordColor @"#57ACFF"
#define recordingColor @"#FF3300"

#define minTime 5000
#define maxTime 15000
#define frameRate 30
#define bitrateConfig 5000000

@implementation RecordView {
  bool _initing;
  bool _isDone;
  bool _isFocused;
}
- (IBAction)btn:(id)sender {
}

-(instancetype) initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher {
  if(self == [super init]) {
    _eventDispatcher = eventDispatcher;
    NSLog(@"recoder init ");
    _initing = NO;
    _isDone = NO;
    _isFocused = YES;
    [self initRecorder];
    [self addObservers];
  }
  return self;
}


-(id) initWithCoder:(NSCoder *) aDecoder {
  return self;
}

// func  sau khi react vẽ view
-(void) reactSetFrame: (CGRect) frame {
  CGPoint position = {CGRectGetMidX(frame), CGRectGetMidY(frame)};
  CGRect bounds = {CGPointZero, frame.size};

  // Avoid crashes due to nan coords
  if (isnan(position.x) || isnan(position.y) ||
      isnan(bounds.origin.x) || isnan(bounds.origin.y) ||
      isnan(bounds.size.width) || isnan(bounds.size.height)) {
    return;
  }

  self.center = position;
  self.bounds = bounds;
  
  
  if(!_initing) {
    _initing = YES;
    [self setPreviewRecorder];
    [self initSession];
    [self startRecorder];
    [self initView];
  }
  
}

-(void) dealloc {
  
  [self removeObservers];
  [self pause];
  [self stopRecorder];
  [self removeAllSegment];
  [self releassRecorder];
  
}


-(void) releassRecorder {
  
  if(_recorder != nil && _recorder.session != nil) {
    [_recorder.session cancelSession:nil];
    _recorder.session = nil;
    [self stopRecorder];
    _recorder = nil;
  }
}

-(void) removeAllSegment {
  if(_recorder != nil && _recorder.session != nil) {
    [_recorder.session removeAllSegments];
  }
}

#pragma app state
-(void) addObservers {
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(applicationWillResignActive:)
                                               name:UIApplicationDidBecomeActiveNotification
                                             object:nil];
  
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(applicationDidEnterBackground:)
                                               name:UIApplicationDidEnterBackgroundNotification
                                             object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self
  selector:@selector(applicationDidEnterBackground:)
      name:UIApplicationWillResignActiveNotification
    object:nil];
}

-(void) removeObservers {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}


// func when app active
- (void)applicationWillResignActive:(NSNotification *)notification
{
  if(_isFocused) {
//    [self startRecorder];
  }
}

// func when app background
- (void)applicationDidEnterBackground:(NSNotification *)notification
{
  if(_isFocused) {
    if(_recorder != nil) {
      [self pause];
      _recorder.flashMode = SCFlashModeOff;
      [self updateBtnFlash];
      [self updateViewRecord];
    }
  }
}

#pragma func
- (void) changeProgessBar: (UIView*)view wid:(int)newWid{
  CGRect rc = view.frame;
  view.frame = CGRectMake(rc.origin.x, rc.origin.y, newWid, rc.size.height);
}

- (void) updateTimeRecorded {
  [self updateBtnDone];
   NSNumber *number = [NSNumber numberWithDouble:(double) [self getDuration] / maxTime * (self.frame.size.width - [self getScaleSizeMin:5])];
  [self changeProgessBar:_progressBar wid:[number intValue]];
  // check done
  if([self getDuration] >= maxTime && !_isDone) {
    [self onDoneAction];
  }
}

- (void) onReshoot {
  if(_recorder != nil) {
    _isDone = false;
    [self updateViewRecord];
    [self removeAllSegment];
    [self initSession];
    [self updateTimeRecorded];
  }
}

- (void) onExitAction {
  [self onReshoot];
  if(self.onExit != nil) {
    self.onExit(nil);
  }
}


- (void) onDoneAction {
  if(!_isDone) {
    [self startSpiner];
    _isDone = YES;
//    [_recorder pause];
    
    [self completeRecord];
  }
}

#pragma action btn

-(IBAction)onDonePress:(id)sender {
  [self onDoneAction];
}

-(IBAction)onClosePress:(id)sender {
  [self onExitAction];
}

-(IBAction)onFlashPress:(id)sender {
  if(_recorder.flashMode == SCFlashModeOff) {
    _recorder.flashMode = SCFlashModeLight;
  }
  else {
    _recorder.flashMode = SCFlashModeOff;
  }
  [self updateBtnFlash];
}

-(IBAction)onSwitchPress:(id)sender {
  if(_recorder.device == AVCaptureDevicePositionFront) {
    _recorder.device = AVCaptureDevicePositionBack;
    _btnFlash.hidden = NO;
  }
  else {
    _recorder.device = AVCaptureDevicePositionFront;
    _recorder.flashMode = SCFlashModeOff;
    _btnFlash.hidden = YES;
    [self updateBtnFlash];
  }
}

-(void) onRecordButtonTap : (UITapGestureRecognizer *)recognizer {
  if(_isDone) return;
  
  if(![_recorder isRecording]) {
    [self record];
  }
  else {
    [self pause];
  }
  [self updateViewRecord];
}

#pragma func update view
- (void) updateBtnFlash {
  
  if(_recorder.flashMode == SCFlashModeOff) {
    [_btnFlash setBackgroundImage:[UIImage imageNamed:@"flash_off"] forState:UIControlStateNormal];
  }
  else {
    [_btnFlash setBackgroundImage:[UIImage imageNamed:@"flash_on"] forState:UIControlStateNormal];
  }
}

- (void) updateViewRecord {
  if([_recorder isRecording]) {
    _topView.hidden = YES;
    [_recordStatusView setBackgroundColor:[self colorWithHexString:recordingColor]];
  }
  else {
    _topView.hidden = NO;
    [_recordStatusView setBackgroundColor:[self colorWithHexString:recordColor]];
  }
}

- (void) updateBtnDone {
  if([self getDuration] >= minTime) {
    _btnDone.hidden = NO;
  }
  else {
    _btnDone.hidden = YES;
  }
}

- (void) startSpiner {
  _spiner.hidden = NO;
  [_spiner startAnimating];
}

-(void) stopSpiner {
  [_spiner stopAnimating];
  _spiner.hidden = YES;
}

- (int) getDuration {
  CMTime currentTime = kCMTimeZero;
  if(_recorder) {
    currentTime = _recorder.session.duration;
  }
  NSLog(@" getDuration %d  ", (int) (CMTimeGetSeconds(currentTime) * 1000));
  return (int) (CMTimeGetSeconds(currentTime) * 1000);
}

#pragma init
// func init Recorder
-(void) initRecorder {
  if(_recorder == nil) {
    _recorder = [SCRecorder recorder];
    _recorder.captureSessionPreset = [SCRecorderTools bestCaptureSessionPresetCompatibleWithAllDevices];
    _recorder.delegate = self;
    _recorder.mirrorOnFrontCamera = YES;
    _recorder.initializeSessionLazily = NO;
    _recorder.device = AVCaptureDevicePositionBack;
    _recorder.videoConfiguration.timeScale = 1;
    
    _recorder.videoConfiguration.size = CGSizeMake(540, 960);
    _recorder.videoConfiguration.bitrate = bitrateConfig;
    _recorder.audioConfiguration.format = kAudioFormatMPEG4AAC;
  }
}

- (void) initSession {
  if(_recorder!= nil) {
    if(_recorder.session != nil) {
      [_recorder.session cancelSession:nil];
      _recorder.session = nil;
    }
    if(_recorder.session == nil) {
      SCRecordSession *session = [SCRecordSession recordSession];
      session.fileType = AVFileTypeMPEG4;
      _recorder.session = session;
    }
  }
}


-(void) initView {
  [self initTopView];
  [self initBottomView];
  [self initProgressBar];
  
}

-(void) initTopView {
  if(_topView == nil) {
    GLfloat width = self.bounds.size.width - [self getMargin] * 2;
    GLfloat realSize = Min([self getScaleSize:sizeIconTop], sizeIconTop);
    _topView = [[UIView alloc] initWithFrame:CGRectMake([self getMargin], [self getTop] , width, realSize)];
    int numberIcon = 3;
    
    GLfloat space = (width - realSize * numberIcon) / (numberIcon - 1);
    
    // add btn close
    _btnClose = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    _btnClose.frame = CGRectMake( (space + realSize) * 0, 0, realSize, realSize);
    [_btnClose setBackgroundImage:[UIImage imageNamed:@"close"] forState:UIControlStateNormal];
    [_btnClose addTarget:self action:@selector(onClosePress:) forControlEvents:UIControlEventTouchUpInside];
    
    [_topView addSubview:_btnClose];
    
    // add btn close
    _btnSwith = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    _btnSwith.frame = CGRectMake( (space + realSize) * 1, 0, realSize, realSize);
    [_btnSwith setBackgroundImage:[UIImage imageNamed:@"switch"] forState:UIControlStateNormal];
    [_btnSwith addTarget:self action:@selector(onSwitchPress:) forControlEvents:UIControlEventTouchUpInside];
    
    [_topView addSubview:_btnSwith];
    
    // add btn close
    _btnFlash = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    _btnFlash.frame = CGRectMake( (space + realSize) * 2, 0, realSize, realSize);
    [_btnFlash setBackgroundImage:[UIImage imageNamed:@"flash_on"] forState:UIControlStateNormal];
    [_btnFlash addTarget:self action:@selector(onFlashPress:) forControlEvents:UIControlEventTouchUpInside];
    [_topView addSubview:_btnFlash];
    
    [self addSubview:_topView];
  }
}

-(void) initBottomView {
  if(_bottomView == nil) {
    GLfloat width = self.bounds.size.width - [self getMargin] * 2;
    GLfloat realSize = Min([self getScaleSizeMin:recordBtnSize], recordBtnSize);
    GLfloat realMargin = [self getMargin];
    _bottomView = [[UIView alloc] initWithFrame:CGRectMake(realMargin, self.bounds.size.height - realMargin * 2 - realSize, width, realMargin * 2 + realSize)];
    
    _recordView = [[UIView alloc] initWithFrame:CGRectMake((_bottomView.bounds.size.width - realSize) / 2, (_bottomView.bounds.size.height  - realSize) / 2, realSize, realSize)];
    
     _recordView.layer.cornerRadius = realSize / 2;
    [_recordView setBackgroundColor:[UIColor whiteColor]];
    
    
    GLfloat realSize2 = [self getScaleSizeMin:recordBtnSize - 10];
    _recordStatusView = [[UIView alloc] initWithFrame:CGRectMake([self getScaleSizeMin:5], [self getScaleSizeMin:5], realSize2, realSize2)];
    _recordStatusView.layer.cornerRadius = realSize2 / 2;
    [_recordStatusView setBackgroundColor:[self colorWithHexString:recordColor]];
    UITapGestureRecognizer *singleFingerTap =
    [[UITapGestureRecognizer alloc] initWithTarget:self
                                            action:@selector(onRecordButtonTap:)];
    
    [_recordView addSubview:_recordStatusView];
    [_recordStatusView addGestureRecognizer:singleFingerTap];
    
    [_bottomView addSubview:_recordView];
    
    // add btn close
    _btnDone = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    
    GLfloat sizeIcon = [self getScaleSizeMin:sizeIconTop * 2];
    _btnDone.frame = CGRectMake( (_bottomView.bounds.size.width / 2 + (_bottomView.bounds.size.width / 2 - sizeIcon - realSize / 2 - [self getMargin]) / 2 + [self getMargin] + realSize / 2 ) , (_bottomView.bounds.size.height - sizeIcon) / 2, sizeIcon , sizeIcon);
    [_btnDone setBackgroundImage:[UIImage imageNamed:@"next"] forState:UIControlStateNormal];
    [_btnDone addTarget:self action:@selector(onDonePress:) forControlEvents:UIControlEventTouchUpInside];
    _btnDone.hidden = YES;
    [_bottomView addSubview:_btnDone];
    
    [self addSubview:_bottomView];
  }
}

-(void) initProgressBar {
  GLfloat margin = [self getScaleSizeMin:5];
  _progressBar = [[UIView alloc] initWithFrame:CGRectMake( margin, _bottomView.frame.origin.y , 0 , [self getScaleSizeMin: 5])];
  [_progressBar setBackgroundColor:[self colorWithHexString:recordColor]];
  _progressBar.layer.cornerRadius = margin / 2;
  [self addSubview:_progressBar];
}

-(void) initSpiner {
  _spiner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
  _spiner.frame = CGRectMake(self.frame.size.width / 2 - 25, self.frame.size.height / 2 - 25, 50, 50);
  
  _spiner.hidden = YES;
  [self addSubview:_spiner];
}

#pragma recoder action
-(void) setPreviewRecorder {
  _recorder.previewView = self;
}

// func bắt đầu recorder
-(void) startRecorder {
  #if !(TARGET_IPHONE_SIMULATOR)
    if(_recorder != nil) {
      [_recorder startRunning];
    }
  #endif
}

// func dừng recorder
-(void) stopRecorder {
  #if !(TARGET_IPHONE_SIMULATOR)
    if(_recorder != nil) {
      [_recorder stopRunning];
    }
  #endif
}

-(void) record {
  if(_recorder != nil) {
    [_recorder record];
  }
}

-(void)pause {
  if(_recorder != nil) {
    [_recorder pause];
  }
}

-(void) completeRecord {
  if(_recorder != nil) {
    [_recorder pause:^{
      [self updateViewRecord];
      [self mergeFile];
    }];
  }
}

-(void) mergeFile {
  AVAsset *asset = _recorder.session.assetRepresentingSegments;
   SCAssetExportSession *assetExportSession = [[SCAssetExportSession alloc] initWithAsset:asset];
   assetExportSession.outputFileType = AVFileTypeMPEG4;
   assetExportSession.videoConfiguration.bitrate = bitrateConfig;
   assetExportSession.videoConfiguration.maxFrameRate = frameRate;
   assetExportSession.outputUrl = _recorder.session.outputUrl;
   [assetExportSession exportAsynchronouslyWithCompletionHandler: ^{
     NSLog(@"onDOne %@", [self -> _recorder.session.outputUrl absoluteURL]);
     [self stopSpiner];
     if(assetExportSession.error == nil) {
       if(self.onDone != nil) {
         
         self.onDone(@{
           @"error" : @(NO),
           @"url": [NSString stringWithFormat:@"%@", [self -> _recorder.session.outputUrl absoluteURL]]
         });
         [self removeAllSegment];
         [self initSession];
       }
       
     }
     else {
       self.onDone(@{
         @"error" : @(YES),
         @"url": @""
       });
       self -> _isDone = NO;
     }
   }];
}

#pragma set props
-(void) setIsFocused:(BOOL *) focused {
  NSLog(@"recoder setfocus %@", focused ? @"true" : @"false");
  if(focused) {
    if(!_isFocused) {
      [self startRecorder];
    }
  }
  else {
    if(_isFocused) {
//      [self initSession];
      [self onReshoot];
      [self stopRecorder];
    }
  }
  _isFocused = focused ? YES : NO;
}

-(void) setConfig:(NSDictionary *) config {
  NSLog(@"recoder setfocus ");
}

#pragma recoder events
- (void)recorder:(SCRecorder *)recorder didAppendVideoSampleBufferInSession:(SCRecordSession *)recordSession {
  [self updateTimeRecorded];
}


- (void)recorder:(SCRecorder *__nonnull)recorder didInitializeVideoInSession:(SCRecordSession *__nonnull)session error:(NSError *__nullable)error{
    NSLog(@"recoder didInitializeVideoInSession ");
}


#pragma util

// func tính sacle size theo từng kích thước
-(GLfloat) getScaleSize: (GLfloat) size {
  CGRect screen = [[UIScreen mainScreen] bounds];
  CGFloat width = CGRectGetWidth(screen);
  
  return self.frame.size.width / width * size;
}

-(GLfloat) getScaleSizeMin: (GLfloat) size {
  CGRect screen = [[UIScreen mainScreen] bounds];
  CGFloat width = CGRectGetWidth(screen);
  
  return Min(self.frame.size.width / width * size, size);
}

// func lấy ra margin phù hợp: max là 24
- (GLfloat) getMargin {
  if([self getScaleSize:marginDefault] > marginDefault) return marginDefault;
  return [self getScaleSize:marginDefault];
}


// func lấy top (tính toán safeAreaView)
- (GLfloat) getTop {
  CGFloat top = 20;
  if (@available(iOS 11.0, *)) {
    if(self.safeAreaInsets.top > 25) {
      top = self.safeAreaInsets.top;
    }
    else {
      top = [self getScaleSize:marginDefault / 2] + self.safeAreaInsets.top;
    }
  } else {
    top = 20 + [self getScaleSize:marginDefault / 2]  ;
  }
  return top;
}

// func lấy bottom (tính toán safeAreaView)
-(GLfloat) getBottom {
  CGFloat bottom = 0;
  if (@available(iOS 11.0, *)) {
    if(self.safeAreaInsets.top > 30) {
      bottom = self.safeAreaInsets.top;
    }
    else {
      bottom = [self getScaleSize:marginDefault / 2] + self.safeAreaInsets.bottom;
    }
  } else {
    bottom = 0 + [self getScaleSize:marginDefault / 2]  ;
  }
  return bottom;
}

- (UIColor *)colorWithHexString:(NSString *)hexString {
  unsigned rgbValue = 0;
  NSScanner *scanner = [NSScanner scannerWithString:hexString];
  [scanner setScanLocation:1]; // bypass '#' character
  [scanner scanHexInt:&rgbValue];
  
  return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}
 
@end
