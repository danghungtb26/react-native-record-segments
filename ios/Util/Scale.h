//
//  Scale.h
//  Recorder
//
//  Created by HungDV on 3/23/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface Scale : NSObject

+(float) getSizeForView:(float) size;

+(Scale *) instance;

@end

NS_ASSUME_NONNULL_END
