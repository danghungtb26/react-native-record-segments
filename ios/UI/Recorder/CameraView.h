//
//  CameraView.h
//  BVLinearGradient
//
//  Created by HungDV on 3/30/20.
//

#import <UIKit/UIKit.h>
#import "React/RCTView.h"
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "React/RCTComponent.h"
#import <React/RCTUIManager.h>
#import "React/RCTView.h"
#import "SCRecorder.h"

NS_ASSUME_NONNULL_BEGIN

@interface CameraView : RCTView <SCRecorderDelegate>


- (id)initWithBridge:(RCTBridge *)bridge;

@property(nonatomic, copy) SCRecorder *recorder;
@property(nonatomic, copy) RCTEventDispatcher *eventDispatcher;
@property(nonatomic, copy) SCRecordSession *session;

@property (nonatomic, copy) RCTBubblingEventBlock onDVExit;
@property (nonatomic, copy) RCTBubblingEventBlock onDoneStart;
@property (nonatomic, copy) RCTBubblingEventBlock onDoneSuccess;
@property (nonatomic, copy) RCTBubblingEventBlock onDVCameraReady;
@property (nonatomic, copy) RCTBubblingEventBlock onDVProgress;
@property (nonatomic, copy) RCTBubblingEventBlock onDVAfterUpdate;

-(void) startRecord;
-(void) pause;
-(void) resume;
-(void) done;
-(BOOL) isInitOrDone;
-(void) flashChange;
-(void) switchChange;
-(BOOL) isRecording;
-(void) capture;
-(void) reshoot;

@end

NS_ASSUME_NONNULL_END
