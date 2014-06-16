declare module a3 {
    class SessionEvent {
        static SESSION_STARTED: string;
        static SESSION_STARTING: string;
        static SESSION_FAILED: string;
        static INCOMING_CALL: string;
    }
    class CallEvent {
        static CALL_STARTING: string;
        static CALL_STARTED: string;
        static CALL_FAILED: string;
        static CALL_FINISHED: string;
        static CALL_TRANSFERED: string;
        static CALL_ERROR: string;
        static SDP_OFFER: string;
        static SDP_ANSWER: string;
    }
    interface ISignalingListener {
        onSignalingReady(o: ISignaling): any;
        onSignalingFailed(o: ISignaling): any;
        onSignalingConnected(o: ISignaling): any;
        onSignalingConnectionFailed(o: ISignaling): any;
        onSignalingMessage(type: string, opt: any): any;
    }
    interface ISignaling {
        start(): any;
        addEndpoint(url: string): any;
        connect(): any;
        request(type: string, opt: Object): any;
        setService(value: string): any;
        setClientInfo(prop: string, value: string): any;
        open(username: string, password: string, challenge: string, code: string): any;
        startCall(bUri: string, cc: Object, vv: boolean[]): any;
        hangup(callId: string): any;
        sdpAnswer(callId: string, pointId: string, sdp: any): any;
        dtmf(callId: string, dtmf: string): any;
    }
    class SioSignaling implements ISignaling {
        private _url;
        private _service;
        private _socket;
        private _listener;
        private _intv;
        public sessionId: string;
        constructor(listener: ISignalingListener);
        public start(): void;
        public setClientInfo(prop: string, value: string): void;
        public addEndpoint(url: string): void;
        public setService(value: string): void;
        public connect(): void;
        public open(username: string, password: string, challenge: string, code: string): void;
        public startCall(bUri: string, cc: Object, vv: boolean[]): void;
        public dtmf(callId: any, dtmf: any): void;
        public hangup(callId: string): void;
        public sdpAnswer(callId: string, pointId: string, sdp: any): void;
        public request(type: string, opt: Object): void;
        public _notifyListener(callback: string, ...args: any[]): void;
    }
    class FlashSignaling implements ISignaling {
        private _listener;
        private _container;
        private _flashVars;
        public sessionId: string;
        private _endpoints;
        private _swf;
        private _service;
        constructor(_listener: ISignalingListener, _container: string, _flashVars: any);
        public start(): void;
        public addEndpoint(url: string): void;
        public setService(value: string): void;
        public connect(): void;
        public close(): void;
        public setClientInfo(prop: any, value: any): void;
        public request(type: string, opt: Object): void;
        public _createOfferFromFlashSignaling(data: any): {
            playUrlVideo: any;
            playUrlVoice: any;
            publishUrlVideo: any;
            publishUrlVoice: any;
        };
        public onSignalingMessage(type: string, data: any): void;
        public open(username: string, password: string, challenge: string, code: string): void;
        public startCall(bUri: string, cc: Object, vv: boolean[]): void;
        public hangup(callId: string): void;
        public dtmf(callId: any, dtmf: any): void;
        public sdpAnswer(callId: string, pointId: string, sdp: any): void;
        public accept(callId: any, av_params: any): void;
        public decline(callId: any): void;
    }
}
declare module a3 {
    class MediaEvent {
        static SDP_ANSWER: string;
    }
    class HardwareState {
        static DISABLED: string;
        static ABSENT: string;
        static ENABLED: string;
    }
    interface IMediaListener {
        onMediaReady(o: IMedia): any;
        onMediaMessage(type: string, opt: any): any;
    }
    interface IMedia {
        start(): any;
        getCc(): any;
        checkHardware(vv: boolean[]): any;
        setMicrophoneVolume(volume: number): any;
        setSoundVolume(volume: number): any;
        muteMicrophone(value: boolean): any;
        muteSound(value: boolean): any;
        setOfferSdp(callId: string, pointId: string, offerSdp: any): any;
        playDtmf(dtmf: string): any;
        playRBT(): any;
        stopRBT(): any;
        dispose(): any;
    }
    class FlashMedia implements IMedia {
        private _listener;
        private _container;
        private _flashVars;
        private _publishUrlVoice;
        private _publishUrlVideo;
        private _playUrlVoice;
        private _playUrlVideo;
        private _swf;
        constructor(_listener: IMediaListener, _container: HTMLElement, _flashVars: any);
        public start(): void;
        public getCc(): any;
        public setMicrophoneVolume(value: any): void;
        public setSoundVolume(value: any): void;
        public muteMicrophone(value: boolean): void;
        public muteSound(value: boolean): void;
        public checkHardware(vv: boolean[]): void;
        public setOfferSdp(callId: string, pointId: string, offerSdp: any): void;
        public dispose(): void;
        public playDtmf(dtmf: string): void;
        public playRBT(): void;
        public stopRBT(): void;
    }
    /**
    * WebrtcMedia
    *   webrtc media implementation
    *
    */
    class WebrtcMedia implements IMedia {
        private _listener;
        private _container;
        private _localStream;
        private _remoteStream;
        private _remoteVideoStream;
        private __pc;
        private __pcVideo;
        private _localVideo;
        private _remoteVideo;
        private _localAudio;
        private _remoteAudio;
        private _soundVolume;
        private _micVolume;
        private _dtmfPlayer;
        private _rbtPlayer;
        constructor(_listener: IMediaListener, _container: any);
        public start(): void;
        public _notifyReady(): void;
        public _notify(type: any, data: any): void;
        public getLocalAudioTrack(): any;
        public setMicrophoneVolume(value: number): void;
        public setSoundVolume(value: number): void;
        public muteMicrophone(value: boolean): void;
        public muteSound(value: boolean): void;
        public getCc(): {
            "userAgent": string;
            "audio": string[];
            "video": string[];
            "profile": string;
            "ice": boolean;
            "rtcpMux": boolean;
            "ssrcRequired": boolean;
            "bundle": boolean;
        };
        public dispose(): void;
        public checkHardware(vv: boolean[]): void;
        public __getAudioPeerConnection(): any;
        public __getVideoPeerConnection(): any;
        public setOfferSdp(callId: string, pointId: string, offerSdp: any): void;
        public playDtmf(dtmf: string): void;
        public playRBT(): void;
        public stopRBT(): void;
    }
}
declare module a3 {
    interface ICommunicatorFactory {
        createMedia(listener: a3.IMediaListener): a3.IMedia;
        createSignaling(listener: a3.ISignalingListener): a3.ISignaling;
    }
    interface ICall {
        setListener(listener: ICallListener): any;
    }
    interface ICallListener {
        onIncomingCall(call: Call): any;
        onCallStarting(call: Call): any;
        onCallStarted(call: Call): any;
        onCallFinished(call: Call): any;
        onCallFailed(call: Call): any;
        onDurationChanged(call: Call, duration: number): any;
    }
    interface ICommunicator extends ICommunicatorListener {
        connect(): any;
        setSoundVolume(value: number): any;
    }
    interface ICommunicatorListener {
        onCommunicatorStarting(): any;
        onCommunicatorStarted(): any;
        onCommunicatorFailed(): any;
        onConnecting(): any;
        onConnected(): any;
        onConnectionFailed(): any;
        onCheckHardwareSettings(): any;
        onCheckHardwareReady(): any;
        onCheckHardwareFailed(): any;
        onSoundVolumeChanged(value: number): any;
        onSessionStarting(): any;
        onSessionStarted(): any;
        onSessionFailed(): any;
    }
    class Event {
        static START: string;
        static SIGNALING_READY: string;
        static SIGNALING_FAILED: string;
        static MEDIA_READY: string;
        static MEDIA_FAILED: string;
        static START_TIMEOUT: string;
        static CONNECT: string;
        static CONNECTED: string;
        static CONNECTION_FAILED: string;
        static HARDWARE_STATE_CHANGED: string;
        static SOUND_VOLUME_CHANGED: string;
        static TRANSFER_FAILED: string;
        static CANCEL: string;
        static START_INCOMING: string;
        static ACCEPT_CALL: string;
        static DECLINE_CALL: string;
    }
    class State {
        static STARTING: string;
        static SIGNALING_READY: string;
        static MEDIA_READY: string;
        static STARTED: string;
        static FAILED: string;
        static CONNECTING: string;
        static CONNECTED: string;
        static CONNECTION_FAILED: string;
        static SESSION_STARTING: string;
        static SESSION_STARTED: string;
        static SESSION_FAILED: string;
        static DISCONNECTED: string;
    }
    class CallState {
        static STARTING: string;
        static RINGING: string;
        static PROGRESS: string;
        static FINISHED: string;
        static FAILED: string;
    }
    class CallType {
        static AUDIO: string;
        static VIDEO: string;
        static BOTH: string;
    }
    class Call implements ICall {
        private _vv;
        private media;
        private signaling;
        private _uid;
        private _state;
        private _duration;
        private _durationTimerHandle;
        private _listener;
        constructor(_vv: any, media: a3.IMedia, signaling: a3.ISignaling);
        public setListener(listener: ICallListener): void;
        public getId(): string;
        public setId(id: any): void;
        public isAudio(): boolean;
        public isVideo(): boolean;
        public accept(): void;
        public decline(): void;
        public start(): void;
        public startIncoming(): void;
        public sendDTMF(dtmf: string): void;
        public getState(): string;
        public hangup(): void;
        public remove(): void;
        public setMicrophoneVolume(value: any): void;
        public setSoundVolume(value: any): void;
        public onEnterStateStarting(): void;
        public onEnterStateRinging(): void;
        public onEnterStateProgress(): void;
        public onEnterStateFinished(): void;
        public onEnterStateFailed(): void;
        public _notifyListener(callback: string): void;
        public _setState(newState: any): void;
        public _unhandledEvent(event: any, opt: any): void;
        public _startTimer(): void;
        public _stopTimer(): void;
        public event(event: string, opt: any): void;
        public __onSdpOffer(opt: any): void;
    }
    class Communicator implements ICommunicator, ICallListener, a3.IMediaListener, a3.ISignalingListener {
        public factory: ICommunicatorFactory;
        public sessionId: string;
        public calls: Call[];
        public signaling: a3.ISignaling;
        public media: a3.IMedia;
        private _startingTimeout;
        private _state;
        private _microphoneState;
        private _cameraState;
        constructor();
        public setFactory(factory: ICommunicatorFactory): void;
        public onCommunicatorStarting(): void;
        public onCommunicatorStarted(): void;
        public onCommunicatorFailed(): void;
        public onConnecting(): void;
        public onConnected(): void;
        public onConnectionFailed(): void;
        public onCheckHardwareSettings(): void;
        public onCheckHardwareReady(): void;
        public onCheckHardwareFailed(): void;
        public onSoundVolumeChanged(value: number): void;
        public onSessionStarting(): void;
        public onSessionStarted(): void;
        public onSessionFailed(): void;
        public onCallStarting(call: Call): void;
        public onCallStarted(call: Call): void;
        public onCallFinished(call: Call): void;
        public onCallFailed(call: Call): void;
        public onIncomingCall(call: Call): void;
        public onDurationChanged(call: Call, duration: number): void;
        public start(): void;
        public connect(): void;
        public open(phone: any, password: any, challenge: any, code: any): void;
        public close(): void;
        public startCall(destination: any, vv: any): void;
        public setSoundVolume(value: number): void;
        public getMicrophoneState(): string;
        public getCameraState(): string;
        public _createCallInstance(vv: boolean[]): Call;
        public _getCallById(id: any): Call;
        public _addCall(call: any): void;
        public _removeCall(call: any): void;
        public _setState(newState: any): void;
        public _setHardwareState(microphoneState: any, cameraState: any): void;
        public getState(): string;
        public _unhandledEvent(event: string, opt: any): void;
        public event(event: string, opt: any): void;
        public _onHardwareStateChanged(opt: any): void;
        public onSignalingReady(o: a3.ISignaling): void;
        public onSignalingFailed(o: a3.ISignaling): void;
        public onSignalingConnected(o: a3.ISignaling): void;
        public onSignalingConnectionFailed(o: a3.ISignaling): void;
        public onSignalingMessage(type: string, opt: any): void;
        public onMediaReady(opt: any): void;
        public onMediaMessage(type: any, opt: any): void;
    }
}
