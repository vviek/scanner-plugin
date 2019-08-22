#import "CsZBar.h"
#import <AVFoundation/AVFoundation.h>
#import "AlmaZBarReaderViewController.h"

#pragma mark - State

@interface CsZBar ()
@property bool scanInProgress;
@property NSString *scanCallbackId;

@property AlmaZBarReaderViewController *scanReader;

@end

#pragma mark - Synthesize

@implementation CsZBar

@synthesize scanInProgress;
@synthesize scanCallbackId;
@synthesize scanReader;

#pragma mark - Cordova Plugin

- (void)pluginInitialize {
    self.scanInProgress = NO;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    return;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

#pragma mark - Plugin API

- (void)scan: (CDVInvokedUrlCommand*)command;
{
    
    if (self.scanInProgress) {
        [self.commandDelegate
         sendPluginResult: [CDVPluginResult
                            resultWithStatus: CDVCommandStatus_ERROR
                            messageAsString:@"A scan is already in progress."]
         callbackId: [command callbackId]];
    } else {
        
        self.scanInProgress = YES;
        self.scanCallbackId = [command callbackId];
        self.scanReader = [AlmaZBarReaderViewController new];
        
        self.scanReader.readerDelegate = self;
        self.scanReader.supportedOrientationsMask = ZBarOrientationMask(UIInterfaceOrientationPortrait);
        // Get user parameters
        NSDictionary *params = (NSDictionary*) [command argumentAtIndex:0];
        NSLog(@"%@", params);
        self.scanReader.buttonVisibilty=[params objectForKey:@"button_two_visibility"];
        
        NSString *camera = [params objectForKey:@"camera"];
        if([camera isEqualToString:@"front"]) {
            // We do not set any specific device for the default "back" setting,
            // as not all devices will have a rear-facing camera.
            self.scanReader.cameraDevice = UIImagePickerControllerCameraDeviceFront;
        }
        self.scanReader.cameraFlashMode = UIImagePickerControllerCameraFlashModeOn;
        
        NSString *flash = [params objectForKey:@"flash"];
        
        if ([flash isEqualToString:@"on"]) {
            self.scanReader.cameraFlashMode = UIImagePickerControllerCameraFlashModeOn;
        } else if ([flash isEqualToString:@"off"]) {
            self.scanReader.cameraFlashMode = UIImagePickerControllerCameraFlashModeOff;
        }else if ([flash isEqualToString:@"auto"]) {
            self.scanReader.cameraFlashMode = UIImagePickerControllerCameraFlashModeAuto;
        }
        
        // Hack to hide the bottom bar's Info button... originally based on http://stackoverflow.com/a/16353530
        NSInteger infoButtonIndex;
        if ([[[UIDevice currentDevice] systemVersion] compare:@"10.0" options:NSNumericSearch] != NSOrderedAscending) {
            infoButtonIndex = 1;
        } else {
            infoButtonIndex = 3;
        }
         UIView *infoButton = [[[[[self.scanReader.view.subviews objectAtIndex:2] subviews] objectAtIndex:0] subviews] objectAtIndex:infoButtonIndex];
         [infoButton setHidden:YES];
         
        //UIButton *button = [UIButton buttonWithType:UIButtonTypeSystem]; [button setTitle:@"Press Me" forState:UIControlStateNormal]; [button sizeToFit]; [self.view addSubview:button];
        CGRect screenRect = [[UIScreen mainScreen] bounds];
        CGFloat screenWidth = screenRect.size.width;
        CGFloat screenHeight = screenRect.size.height;
        
        BOOL drawSight = [params objectForKey:@"drawSight"] ? [[params objectForKey:@"drawSight"] boolValue] : true;
        UIToolbar *toolbarViewFlash = [[UIToolbar alloc] init];
        
        //The bar length it depends on the orientation
        toolbarViewFlash.frame = CGRectMake(0.0, 0, (screenWidth > screenHeight ?screenWidth:screenHeight), 44.0);
        toolbarViewFlash.barStyle = UIBarStyleBlackOpaque;
        UIBarButtonItem *buttonFlash = [[UIBarButtonItem alloc] initWithTitle:@"Flash" style:UIBarButtonItemStyleDone target:self action:@selector(toggleflash)];
        
        /*UIBarButtonItem *buttonTag = [[UIBarButtonItem alloc] initWithTitle:@"Tag" style:UIBarButtonItemStyleDone target:self action:@selector(toggleflash)];
         
         UIBarButtonItem *buttonUnTag = [[UIBarButtonItem alloc] initWithTitle:@"UnTag" style:UIBarButtonItemStyleDone target:self action:@selector(toggleflash)];
         */
        
        NSArray *buttons = [NSArray arrayWithObjects: buttonFlash, nil];
        [toolbarViewFlash setItems:buttons animated:NO];
        [self.scanReader.view addSubview:toolbarViewFlash];
        
        
        NSString *text_one = [params objectForKey:@"text_one"];
        NSString *text_two = [params objectForKey:@"text_two"];
        NSString *text_three = [params objectForKey:@"text_three"];
        
        
        UILabel *labelOne = [[UILabel alloc]initWithFrame:CGRectMake(0,screenRect.size.height*0.60, screenRect.size.width*(1), screenRect.size.height*0.15)];
        labelOne.text = text_one;
        labelOne.numberOfLines = 1;
        labelOne.baselineAdjustment = UIBaselineAdjustmentAlignBaselines;
        labelOne.minimumScaleFactor = 10.0f/12.0f;
        labelOne.clipsToBounds = YES;
        labelOne.backgroundColor = [UIColor clearColor];
        labelOne.textColor = [UIColor whiteColor];
        labelOne.textAlignment = NSTextAlignmentCenter;
        [self.scanReader.view addSubview:labelOne];
        
        
        UILabel *labelTwo = [[UILabel alloc]initWithFrame:CGRectMake(0,screenRect.size.height*0.65, screenRect.size.width*(1), screenRect.size.height*0.15)];
        labelTwo.text = text_two;
        labelTwo.numberOfLines = 1;
        labelTwo.baselineAdjustment = UIBaselineAdjustmentAlignBaselines;
        labelTwo.minimumScaleFactor = 10.0f/12.0f;
        labelTwo.clipsToBounds = YES;
        labelTwo.backgroundColor = [UIColor clearColor];
        labelTwo.textColor = [UIColor whiteColor];
        labelTwo.textAlignment = NSTextAlignmentCenter;
        [self.scanReader.view addSubview:labelTwo];
        
        
        UILabel *labelThree = [[UILabel alloc]initWithFrame:CGRectMake(0,screenRect.size.height*0.70, screenRect.size.width*(1), screenRect.size.height*0.15)];
        labelThree.text = text_three;
        labelThree.numberOfLines = 1;
        labelThree.baselineAdjustment = UIBaselineAdjustmentAlignBaselines;
        labelThree.minimumScaleFactor = 10.0f/12.0f;
        labelThree.clipsToBounds = YES;
        labelThree.backgroundColor = [UIColor clearColor];
        labelThree.textColor = [UIColor whiteColor];
        labelThree.textAlignment = NSTextAlignmentCenter;
        [self.scanReader.view addSubview:labelThree];
        
        
        UIButton *button = [UIButton buttonWithType:UIButtonTypeRoundedRect];
        
        [button setTitle:@"UnTaggable" forState:UIControlStateNormal];
        [button sizeToFit];
        
        
        //[button setContentEdgeInsets:UIEdgeInsetsMake(20, 30, 20, 30)];
        button.contentHorizontalAlignment=UIControlContentHorizontalAlignmentCenter;
        CGRect frame;
        
        if (screenRect.size.height > (screenRect.size.width)) {
            frame = CGRectMake(0,screenRect.size.height*0.75, screenRect.size.width*(1), screenRect.size.height*0.15);
        }else{
            frame = CGRectMake(0,screenRect.size.height*0.75, screenRect.size.width*(1), screenRect.size.height*0.20);
        }
        
        button.frame =frame;
        button.layer.cornerRadius = 10;
        button.clipsToBounds = YES;
        if([self.scanReader.buttonVisibilty isEqualToString:@"true"])
        {
            button.hidden=NO;
        }else{
            button.hidden=YES;
        }
        [button addTarget:self action:@selector(okButtonTapped:) forControlEvents:UIControlEventTouchUpInside];
        
        [self.scanReader.view addSubview:button];
        
        
        
        
        
        
        
        if (drawSight) {
            CGFloat dim = screenWidth < screenHeight ? screenWidth / 1.1 : screenHeight / 1.1;
            UIView *polygonView = [[UIView alloc] initWithFrame: CGRectMake  ( (screenWidth/2) - (dim/2), (screenHeight/2) - (dim/2), dim, dim)];
            
            UIView *lineView = [[UIView alloc] initWithFrame:CGRectMake(0,dim / 2, dim, 1)];
            lineView.backgroundColor = [UIColor redColor];
            [polygonView addSubview:lineView];
            
            self.scanReader.cameraOverlayView = polygonView;
            
        }
        
        [self.viewController presentViewController:self.scanReader animated:YES completion:nil];
    }
}

- (void)toggleflash {
    AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    
    [device lockForConfiguration:nil];
    if (device.torchAvailable == 1) {
        if (device.torchMode == 0) {
            [device setTorchMode:AVCaptureTorchModeOn];
            [device setFlashMode:AVCaptureFlashModeOn];
        } else {
            [device setTorchMode:AVCaptureTorchModeOff];
            [device setFlashMode:AVCaptureFlashModeOff];
        }
    }
    
    [device unlockForConfiguration];
}

#pragma mark - Helpers

- (void)sendScanResult: (CDVPluginResult*)result {
    [self.commandDelegate sendPluginResult: result callbackId: self.scanCallbackId];
}

#pragma mark - ZBarReaderDelegate

- (void) imagePickerController:(UIImagePickerController *)picker didFinishPickingImage:(UIImage *)image editingInfo:(NSDictionary *)editingInfo {
    return;
}

- (void)imagePickerController:(UIImagePickerController*)picker didFinishPickingMediaWithInfo:(NSDictionary*)info {
    if ([self.scanReader isBeingDismissed]) {
        return;
    }
    
    id<NSFastEnumeration> results = [info objectForKey: ZBarReaderControllerResults];
    
    ZBarSymbol *symbol = nil;
    for (symbol in results) break; // get the first result
    
    [self.scanReader dismissViewControllerAnimated: YES completion: ^(void) {
        self.scanInProgress = NO;
        [self sendScanResult: [CDVPluginResult
                               resultWithStatus: CDVCommandStatus_OK
                               messageAsString: symbol.data]];
    }];
}

- (void) imagePickerControllerDidCancel:(UIImagePickerController*)picker {
    [self.scanReader dismissViewControllerAnimated: YES completion: ^(void) {
        self.scanInProgress = NO;
        [self sendScanResult: [CDVPluginResult
                               resultWithStatus: CDVCommandStatus_ERROR
                               messageAsString: @"cancelled"]];
    }];
}

- (void) readerControllerDidFailToRead:(ZBarReaderController*)reader withRetry:(BOOL)retry {
    [self.scanReader dismissViewControllerAnimated: YES completion: ^(void) {
        self.scanInProgress = NO;
        [self sendScanResult: [CDVPluginResult
                               resultWithStatus: CDVCommandStatus_ERROR
                               messageAsString: @"Failed"]];
    }];
}


- (void)okButtonTapped:(UIButton *)sender {
    [self.scanReader dismissViewControllerAnimated: YES completion: ^(void) {
        self.scanInProgress = NO;
        [self sendScanResult: [CDVPluginResult
                               resultWithStatus: CDVCommandStatus_ERROR
                               messageAsString: @"null:untag"]];
    }];
}
@end
