//
//  SCFilterImageView.m
//  SCRecorder
//
//  Created by Simon Corsin on 10/8/15.
//  Copyright © 2015 rFlex. All rights reserved.
//

#import "SCFilterImageView.h"

@implementation SCFilterImageView

- (CIImage *)renderedCIImageInRect:(CGRect)rect {
    CIImage *image = [super renderedCIImageInRect:rect];
    CFTimeInterval imageTime = self.CIImageTime;
    if (image != nil) {
        if (_filter != nil) {
            image = [_filter imageByProcessingImage:image atTime:imageTime];
        }
    }

    return image;
}

- (void)setFilter:(SCFilter *)filter {
    _filter = filter;
    [self loadContextIfNeeded];
    [self setNeedsDisplay];
}

@end
