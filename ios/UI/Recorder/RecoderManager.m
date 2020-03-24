//
//  RecoderManager.m
//  RNFFmpegRecorder
//
//  Created by HungDV on 3/23/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "RecoderManager.h"
#import "React/RCTBridge.h"
#import <React/RCTLog.h>

#import "React/RCTEventDispatcher.h"
#import "React/UIView+React.h"
#import "RecordView.h"

@implementation RecoderManager


@synthesize bridge = _bridge;

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE(RNRecord);
RCT_EXPORT_VIEW_PROPERTY(config, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(isFocused, BOOL);


RCT_EXPORT_VIEW_PROPERTY(onDone, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onExit, RCTBubblingEventBlock);

-(UIView *) view {
  return [[RecordView alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
}

@end
