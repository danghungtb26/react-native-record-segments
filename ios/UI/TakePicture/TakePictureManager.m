//
//  TakePictureManager.m
//  Recorder
//
//  Created by HungDV on 3/24/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "TakePictureManager.h"
#import "TakePictureView.h"

#import "React/RCTBridge.h"
#import <React/RCTLog.h>

#import "React/RCTEventDispatcher.h"
#import "React/UIView+React.h"

@implementation TakePictureManager

@synthesize bridge = _bridge;

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE(RNTakePicture);
RCT_EXPORT_VIEW_PROPERTY(isFocused, BOOL);


RCT_EXPORT_VIEW_PROPERTY(onTakeDone, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onTakeExit, RCTBubblingEventBlock);

-(UIView *) view {
  return [[TakePictureView alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
}

@end
