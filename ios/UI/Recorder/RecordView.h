//
//  RecordView.h
//  RNFFmpegRecorder
//
//  Created by HungDV on 3/23/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "React/RCTView.h"
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "React/RCTComponent.h"
#import "React/RCTView.h"
#import "SCRecorder.h"

@class RCTEventDispatcher;

NS_ASSUME_NONNULL_BEGIN

@interface RecordView : RCTView <SCRecorderDelegate>

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;

@property(nonatomic, copy) SCRecorder *recorder;
@property(nonatomic, copy) RCTEventDispatcher *eventDispatcher;
@property(nonatomic, copy) SCRecordSession *session;
@property(nonatomic, copy) UIView *preview;
@property(nonatomic, copy) NSString *device;
@property(nonatomic, assign) NSInteger *flashMode;

// View
@property(nonatomic, copy) UIView *topView;
@property(nonatomic, copy) UIView *bottomView;
@property(nonatomic, copy) UIView *recordView;
@property(nonatomic, copy) UIView *recordStatusView;

@property(nonatomic, copy) UIButton *btnClose;
@property(nonatomic, copy) UIButton *btnFlash;
@property(nonatomic, copy) UIButton *btnSwith;
@property(nonatomic, copy) UIButton *btnDone;

@property(nonatomic, copy) UIView *progressBar;
@property(nonatomic, copy) UIActivityIndicatorView *spiner;

@property (nonatomic, copy) RCTBubblingEventBlock onDone;
@property (nonatomic, copy) RCTBubblingEventBlock onExit;
//

@end

NS_ASSUME_NONNULL_END
