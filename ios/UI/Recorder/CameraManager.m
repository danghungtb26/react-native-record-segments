//
//  CameraManager.m
//  
//
//  Created by HungDV on 3/31/20.
//

#import "CameraManager.h"
#import "React/RCTBridge.h"
#import <React/RCTLog.h>
#import <React/RCTUIManager.h>
#import "React/RCTEventDispatcher.h"
#import "React/UIView+React.h"
#import "CameraView.h"

@implementation CameraManager

- (dispatch_queue_t)methodQueue
{
    return self.bridge.uiManager.methodQueue;
//  return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE(RNDVCamera);
RCT_EXPORT_VIEW_PROPERTY(config, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(isFocused, BOOL);


RCT_EXPORT_VIEW_PROPERTY(onDVExit, RCTBubblingEventBlock);

RCT_EXPORT_VIEW_PROPERTY(onDoneStart, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onDoneSuccess, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onDVCameraReady, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onDVProgress, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onDVAfterUpdate, RCTBubblingEventBlock);

-(UIView *) view {
  return [[CameraView alloc] initWithBridge:self.bridge];
}

RCT_EXPORT_METHOD(record:(nonnull NSNumber *)reactTag)
{
    #if TARGET_IPHONE_SIMULATOR
        return;
    #endif
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, CameraView *> *viewRegistry) {
        CameraView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[CameraView class]]) {
        } else {
            [view startRecord];
        }
    }];
}

RCT_EXPORT_METHOD(pause:(nonnull NSNumber *)reactTag)
{
    #if TARGET_IPHONE_SIMULATOR
        return;
    #endif
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, CameraView *> *viewRegistry) {
        CameraView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[CameraView class]]) {
        } else {
            [view pause];
        }
    }];
}

RCT_EXPORT_METHOD(resume:(nonnull NSNumber *)reactTag)
{
    #if TARGET_IPHONE_SIMULATOR
        return;
    #endif
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, CameraView *> *viewRegistry) {
        CameraView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[CameraView class]]) {
        } else {
            [view resume];
        }
    }];
}

RCT_EXPORT_METHOD(done:(nonnull NSNumber *)reactTag)
{
    #if TARGET_IPHONE_SIMULATOR
        return;
    #endif
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, CameraView *> *viewRegistry) {
        CameraView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[CameraView class]]) {
        } else {
            [view done];
        }
    }];
}

RCT_EXPORT_METHOD(capture:(nonnull NSNumber *)reactTag)
{
    #if TARGET_IPHONE_SIMULATOR
        return;
    #endif
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, CameraView *> *viewRegistry) {
        CameraView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[CameraView class]]) {
        } else {
            [view capture];
        }
    }];
}

RCT_EXPORT_METHOD(checkInitOrDone:(nonnull NSNumber *)reactTag
                        resolver:(RCTPromiseResolveBlock)resolve
                        rejecter:(RCTPromiseRejectBlock)reject)
{
    #if TARGET_IPHONE_SIMULATOR
        return;
    #endif
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, CameraView *> *viewRegistry) {
        CameraView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[CameraView class]]) {
            
        } else {
            resolve(@([view isInitOrDone]));
        }
    }];
}

RCT_EXPORT_METHOD(isRecording:(nonnull NSNumber *)reactTag
                        resolver:(RCTPromiseResolveBlock)resolve
                        rejecter:(RCTPromiseRejectBlock)reject)
{
    #if TARGET_IPHONE_SIMULATOR
        return;
    #endif
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, CameraView *> *viewRegistry) {
        CameraView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[CameraView class]]) {
            
        } else {
            resolve(@([view isRecording]));
        }
    }];
}

RCT_EXPORT_METHOD(changeFlash:(nonnull NSNumber *)reactTag)
{
    #if TARGET_IPHONE_SIMULATOR
        return;
    #endif
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, CameraView *> *viewRegistry) {
        CameraView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[CameraView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting RNCamera, got: %@", view);
        } else {
            [view flashChange];
        }
    }];
}

RCT_EXPORT_METHOD(changeSwitch:(nonnull NSNumber *)reactTag)
{
    #if TARGET_IPHONE_SIMULATOR
        return;
    #endif
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, CameraView *> *viewRegistry) {
        CameraView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[CameraView class]]) {
        } else {
            [view switchChange];
        }
    }];
}

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

@end
