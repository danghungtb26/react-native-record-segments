//
//  CameraView.m
//  BVLinearGradient
//
//  Created by HungDV on 3/30/20.
//

#import "CameraView.h"

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

@implementation CameraView {
  bool _initing;
  bool _isDone;
  bool _isFocused;
}

-(id) initWithBridge:(RCTBridge *)bridge {
  if(self == [super init]) {
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
        [self callbackAfterUpdate];
    }
  }
}

#pragma void
-(void) startRecord {
    [self record];
}
-(void) resume {
    [self record];
}
-(void) done {
    [self onDoneAction];
}
-(BOOL) isInitOrDone {
    return _initing || _isDone;
}

-(BOOL) isRecording {
    return [_recorder isRecording];
}

-(void) reshoot {
    [self onReshoot];
}

-(void) capture {
    if(_isDone) {
        return;
    }
    _isDone = YES;
    [self callbackAfterUpdate];
    if(self.onDoneStart) {
        self.onDoneStart(nil);
    }
    UIImage *image = [_recorder snapshotOfLastVideoBuffer];
    NSString *imgPath = [self saveImage:[self scaleImage:image]];
    NSLog(@"imgPath %@", imgPath);
    if(self.onDoneSuccess) {
            self.onDoneSuccess(@{
              @"error:": @(NO),
              @"url": imgPath,
              @"type": @"image",
            });
          }
}

- (NSString*)saveImage:(UIImage*)image
{
   BOOL isDir = true;
       NSFileManager *fileManager= [NSFileManager defaultManager];
       if(![fileManager fileExistsAtPath:[NSString stringWithFormat:@"%@/record",NSTemporaryDirectory()] isDirectory:&isDir])
         if(![[NSFileManager defaultManager] createDirectoryAtPath:[NSTemporaryDirectory() stringByAppendingFormat:@"record"] withIntermediateDirectories:YES attributes:nil error:NULL]) {
           NSLog(@"Error: Create folder failed ");
         }
       NSLog(@"folder %@", [NSString stringWithFormat:@"%@/record",NSTemporaryDirectory()] );
       
   //    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
       NSTimeInterval timeStamp = [[NSDate date] timeIntervalSince1970];
           NSInteger time = timeStamp;
           NSString *fileName = [NSString stringWithFormat: @"%ld.jpg", (long)time];
       
       NSString *name = [[NSProcessInfo processInfo] globallyUniqueString];
       name = [name stringByAppendingString:@".jpg"];
       NSString *filePath = [NSTemporaryDirectory() stringByAppendingPathComponent:[NSString stringWithFormat:@"record/%@", fileName]];
       
       
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

-(void) callbackAfterUpdate {
    if(self.onDVAfterUpdate) {
        self.onDVAfterUpdate(@{
            @"isIniting": @(_initing),
            @"isDone": @(_isDone),
            @"isRecording": @([_recorder isRecording]),
            @"isFlashOn": @(_recorder.flashMode == SCFlashModeLight),
            @"isDeviceBack": @(_recorder.device == AVCaptureDevicePositionBack),
            @"isRecorded": @([self getDuration] > 0 || [_recorder isRecording])
        });
    }
}

#pragma func
- (void) changeProgessBar: (UIView*)view wid:(int)newWid{
  CGRect rc = view.frame;
  view.frame = CGRectMake(rc.origin.x, rc.origin.y, newWid, rc.size.height);
}

- (void) updateTimeRecorded {
    if(self.onDVProgress) {
        self.onDVProgress(@{
            @"progress": @([self getDuration])
        });
    }
  // check done
  if([self getDuration] >= maxTime && !_isDone) {
    [self onDoneAction];
  }
}

- (int) getDuration {
  CMTime currentTime = kCMTimeZero;
  if(_recorder) {
    currentTime = _recorder.session.duration;
  }
  NSLog(@" getDuration %d  ", (int) (CMTimeGetSeconds(currentTime) * 1000));
  return (int) (CMTimeGetSeconds(currentTime) * 1000);
}

- (void) onReshoot {
  if(_recorder != nil) {
    _isDone = false;
    [self callbackAfterUpdate];
    [self removeAllSegment];
    [self initSession];
    [self updateTimeRecorded];
  }
}

- (void) onExitAction {
  [self onReshoot];
  if(self.onDVExit != nil) {
    self.onDVExit(nil);
  }
}


- (void) onDoneAction {
  if(!_isDone) {
    _isDone = YES;
//    [_recorder pause: nil];
    
    [self completeRecord];
  }
}

-(void)flashChange {
  if(_recorder.flashMode == SCFlashModeOff) {
    _recorder.flashMode = SCFlashModeLight;
  }
  else {
    _recorder.flashMode = SCFlashModeOff;
  }
    [self callbackAfterUpdate];
}

-(void)switchChange {
  if(_recorder.device == AVCaptureDevicePositionFront) {
    _recorder.device = AVCaptureDevicePositionBack;
  }
  else {
    _recorder.device = AVCaptureDevicePositionFront;
    _recorder.flashMode = SCFlashModeOff;
  }
    [self callbackAfterUpdate];
}

-(void) onRecordButtonTap : (UITapGestureRecognizer *)recognizer {
  if(_isDone) return;
  
  if(![_recorder isRecording]) {
    [self record];
  }
  else {
    [self pause];
  }
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
  
}


#pragma recoder action
-(void) setPreviewRecorder {
  _recorder.previewView = self;
    if(self.onDVCameraReady) {
        self.onDVCameraReady(nil);
    }
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
      [self callbackAfterUpdate];
  }
}

-(void) pause {
  if(_recorder != nil) {
      [_recorder pause:^{
          [self callbackAfterUpdate];
      }];
    
  }
}

-(void) completeRecord {
  if(_recorder != nil) {
    [_recorder pause:^{
      [self callbackAfterUpdate];
      [self mergeFile];
    }];
  }
}

-(void) mergeFile {
    if(self.onDoneStart) {
        self.onDoneStart(nil);
    }
    AVAsset *asset = _recorder.session.assetRepresentingSegments;
   SCAssetExportSession *assetExportSession = [[SCAssetExportSession alloc] initWithAsset:asset];
   assetExportSession.outputFileType = AVFileTypeMPEG4;
   assetExportSession.videoConfiguration.bitrate = bitrateConfig;
   assetExportSession.videoConfiguration.maxFrameRate = frameRate;
   assetExportSession.outputUrl = _recorder.session.outputUrl;
   [assetExportSession exportAsynchronouslyWithCompletionHandler: ^{
     NSLog(@"onDOne %@", [self -> _recorder.session.outputUrl absoluteURL]);
     if(assetExportSession.error == nil) {
       if(self.onDoneSuccess != nil) {
         
         self.onDoneSuccess(@{
           @"error" : @(NO),
           @"url": [NSString stringWithFormat:@"%@", [self -> _recorder.session.outputUrl absoluteURL]],
           @"type": @"video"
         });
         [self removeAllSegment];
         [self initSession];
       }
       
     }
     else {
       self.onDoneSuccess(@{
         @"error" : @(YES),
         @"url": @"",
         @"type": @"video"
       });
       [self callbackAfterUpdate];
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
 
@end

