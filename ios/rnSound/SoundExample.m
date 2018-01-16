//
//  SoundExample.m
//  rnSound
//
//  Created by Richard Patsch on 12.01.18.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SoundExample.h"
#import <React/RCTLog.h>
#import <React/RCTConvert.h>
#import "STKAudioPlayer.h"
#import <AVFoundation/AVFoundation.h>
@import MediaPlayer;

#define MEDIA_SPEED @"speed"
#define MEDIA_DICT @{@"album": MPMediaItemPropertyAlbumTitle, \
  @"trackCount": MPMediaItemPropertyAlbumTrackCount, \
  @"trackNumber": MPMediaItemPropertyAlbumTrackNumber, \
  @"artist": MPMediaItemPropertyArtist, \
  @"composer": MPMediaItemPropertyComposer, \
  @"discCount": MPMediaItemPropertyDiscCount, \
  @"discNumber": MPMediaItemPropertyDiscNumber, \
  @"genre": MPMediaItemPropertyGenre, \
  @"persistentID": MPMediaItemPropertyPersistentID, \
  @"duration": MPMediaItemPropertyPlaybackDuration, \
  @"title": MPMediaItemPropertyTitle, \
  @"elapsedTime": MPNowPlayingInfoPropertyElapsedPlaybackTime, \
  MEDIA_SPEED: MPNowPlayingInfoPropertyPlaybackRate, \
  @"playbackQueueIndex": MPNowPlayingInfoPropertyPlaybackQueueIndex, \
  @"playbackQueueCount": MPNowPlayingInfoPropertyPlaybackQueueCount, \
  @"chapterNumber": MPNowPlayingInfoPropertyChapterNumber, \
  @"chapterCount": MPNowPlayingInfoPropertyChapterCount \
}


@implementation SoundExample

STKAudioPlayer* audioPlayer;

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(startService){
  NSError* error;
  AVAudioSession *session = [AVAudioSession sharedInstance];
  [session setCategory:AVAudioSessionCategoryPlayback error:&error];
  [session setActive:YES error:&error];
  
  STKAudioPlayerOptions audioOptions;
  memset(&audioOptions, 0, sizeof(audioOptions));
  audioOptions.enableVolumeMixer = YES;
  audioPlayer = [[STKAudioPlayer alloc] initWithOptions:audioOptions];

  
  dispatch_async(dispatch_get_main_queue(), ^{
    [[UIApplication sharedApplication] beginReceivingRemoteControlEvents];
  });
  
 
}

RCT_EXPORT_METHOD(play:(NSDictionary *)details){
  NSString *url = [RCTConvert NSString:details [@"url"]];
  NSString *title = [RCTConvert NSString:details [@"title"]];
  NSString *subtitle =[RCTConvert NSString:details [@"subtitle"]];
  
  RCTLogInfo(@"url is %@", url);
  RCTLogInfo(@"title is %@", title);
  RCTLogInfo(@"subtitle is %@", subtitle);

  [audioPlayer play:url];
  
  MPRemoteCommandCenter *remoteCenter = [MPRemoteCommandCenter sharedCommandCenter];
  remoteCenter.pauseCommand.enabled = true;
  [remoteCenter.playCommand addTarget:self action:@selector(onPlay:)];
  remoteCenter.playCommand.enabled = true;
  [remoteCenter.pauseCommand addTarget:self action:@selector(onPause:)];
  
  NSDictionary *infos = [NSMutableDictionary dictionary];
  [infos setValue:title forKey: MPMediaItemPropertyTitle];
  [infos setValue:subtitle forKey: MPMediaItemPropertyArtist];
  
  for (NSString *key in MEDIA_DICT) {
    NSLog(key);
  }
  
  dispatch_async(dispatch_get_main_queue(), ^{
    MPNowPlayingInfoCenter *center = [MPNowPlayingInfoCenter defaultCenter];
    //[MEDIA_DICT setValue:@"THE TITLE" forKey:MPMediaItemPropertyTitle];
    center.nowPlayingInfo = infos;
  });
}

RCT_EXPORT_METHOD(pause) {
  [self pauseMethod];
}

RCT_EXPORT_METHOD(resume) {
  [self resumeMethod];
}

- (void) pauseMethod {
  [audioPlayer pause];
}

- (void) resumeMethod {
  [audioPlayer resume];
}

- (void)onPlay:(MPRemoteCommandHandlerStatus*)event {
  NSLog(@"play");
  [self resumeMethod];
}

- (void) onPause:(MPRemoteCommandHandlerStatus*)event {
  NSLog(@"pause");
  [self pauseMethod];
}

//stolen method
- (NSDictionary *) update:(NSMutableDictionary *) mediaDict with:(NSDictionary *) details andSetDefaults:(BOOL) setDefault {
  
  for (NSString *key in MEDIA_DICT) {
    if ([details objectForKey:key] != nil) {
      [mediaDict setValue:[details objectForKey:key] forKey:[MEDIA_DICT objectForKey:key]];
    }
    
    // In iOS Simulator, always include the MPNowPlayingInfoPropertyPlaybackRate key in your nowPlayingInfo dictionary
    // only if we are creating a new dictionary
    if ([key isEqualToString:MEDIA_SPEED] && [details objectForKey:key] == nil && setDefault) {
      [mediaDict setValue:[NSNumber numberWithDouble:1] forKey:[MEDIA_DICT objectForKey:key]];
    }
  }
  
  return mediaDict;
}


@end
