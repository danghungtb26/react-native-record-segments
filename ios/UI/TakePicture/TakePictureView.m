//
//  TakePictureView.m
//  Recorder
//
//  Created by HungDV on 3/24/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "TakePictureView.h"

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

@implementation TakePictureView {
  bool _initing;
  bool _isFocused;
  bool _isTake;
}


-(instancetype) initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher {
  if(self == [super init]) {
    _eventDispatcher = eventDispatcher;
    NSLog(@"recoder init ");
    _initing = NO;
    _isTake = NO;
    _isFocused = YES;
    [self initRecorder];
    [self addObservers];
  }
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
    [self startRecorder];
    [self initView];
  }
  
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
      _isTake = NO;
      [self stopRecorder];
    }
  }
  _isFocused = focused ? YES : NO;
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
      _recorder.flashMode = SCFlashModeOff;
      [self updateBtnFlash];
    }
  }
}

-(void) dealloc {
  
  [self removeObservers];
  [self pause];
  [self stopRecorder];
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

- (void) onExitAction {
  if(self.onTakeExit != nil) {
    self.onTakeExit(nil);
  }
}

#pragma action btn


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

- (NSString*)saveImage:(UIImage*)image
{
   NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
   NSString *name = [[NSProcessInfo processInfo] globallyUniqueString];
   name = [name stringByAppendingString:@".jpg"];
   NSString *filePath = [[paths objectAtIndex:0] stringByAppendingPathComponent:name];

   [UIImagePNGRepresentation(image) writeToFile:filePath atomically:YES];
   return filePath;
}


- (UIImage *)scaleImage:(UIImage *)image  {
    CGSize actSize = image.size;


    UIGraphicsBeginImageContext(CGSizeMake(540, 960));
    [image drawInRect:CGRectMake(0, 0, 540, 960)];
    UIImage* newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    return [UIImage imageWithData:UIImageJPEGRepresentation(newImage, 20)];
}

-(void) onRecordButtonTap : (UITapGestureRecognizer *)recognizer {
  if(_isTake) return;
  _isTake = true;
  UIImage *image = [_recorder snapshotOfLastVideoBuffer];
  NSString *imgPath = [self saveImage:[self scaleImage:image]];
  NSLog(@"imgPath %@", imgPath);
  if(self.onTakeDone) {
          self.onTakeDone(@{
            @"error:": @(NO),
            @"url": imgPath
          });
        }
  

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

-(void) initRecorder {
  if(_recorder == nil) {
    _recorder = [SCRecorder recorder];
    _recorder.captureSessionPreset = [SCRecorderTools bestCaptureSessionPresetCompatibleWithAllDevices];
    _recorder.mirrorOnFrontCamera = YES;
    _recorder.initializeSessionLazily = NO;
    _recorder.device = AVCaptureDevicePositionBack;
    
//    _recorder.videoConfiguration.enabled = NO;
    _recorder.audioConfiguration.enabled = NO;
    _recorder.photoConfiguration.enabled = YES;
    
  }
}


-(void) initView {
  [self initTopView];
  [self initBottomView];
  
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
//    _btnDone = [UIButton buttonWithType:UIButtonTypeRoundedRect];
//
//    GLfloat sizeIcon = [self getScaleSizeMin:sizeIconTop * 2];
//    _btnDone.frame = CGRectMake( (_bottomView.bounds.size.width / 2 + (_bottomView.bounds.size.width / 2 - sizeIcon - realSize / 2 - [self getMargin]) / 2 + [self getMargin] + realSize / 2 ) , (_bottomView.bounds.size.height - sizeIcon) / 2, sizeIcon , sizeIcon);
//    [_btnDone setBackgroundImage:[UIImage imageNamed:@"next"] forState:UIControlStateNormal];
//    [_btnDone addTarget:self action:@selector(onDonePress:) forControlEvents:UIControlEventTouchUpInside];
//    _btnDone.hidden = YES;
//    [_bottomView addSubview:_btnDone];
    
    [self addSubview:_bottomView];
  }
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
