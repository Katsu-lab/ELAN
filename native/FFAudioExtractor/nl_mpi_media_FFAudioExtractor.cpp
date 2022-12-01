#include "nl_mpi_media_FFAudioExtractor.h"
#include "mpi_jni_util.h"
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif
#include "FFAudioExtractor.h"
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>

	/*
	 * Initializes an FFMPEG AudioExtractor and links it to a global reference to the caller class.
	 * Detects properties of the audio track(s) of the file and creates a decoder context and other
	 * types of objects necessary for extraction of audio samples.
	 *
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    initNativeFF
	 * Signature: (Ljava/lang/String;)J
	 */
	JNIEXPORT jlong JNICALL Java_nl_mpi_media_FFAudioExtractor_initNativeFF
	(JNIEnv *env, jobject callerObj, jstring mediaPath) {
		jlong ae_id = 0;
	    // convert string
        const char *mediaURLChars = env->GetStringUTFChars(mediaPath, NULL);
		mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: creating audio extractor for: %s", mediaURLChars);
		int result = 0;
	    AVFormatContext *fmt_ctx = NULL;
	    
	    // open input file, and allocate format context /
	    result = avformat_open_input(&fmt_ctx, mediaURLChars, NULL, NULL);
	    if (result < 0) {
	    	mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: could not open the input media file (err. %d)", result);
	    	// clean up and return
	    	env->ReleaseStringUTFChars(mediaPath, mediaURLChars);
		    return 0;
	    }
		//
	    AVCodecContext *audio_dec_ctx = NULL;
	    int audio_stream_idx = -1;
	    AVStream *audio_stream = NULL;// necessary?
	    // retrieve stream information //
	    if (result >= 0) {
	    	result = avformat_find_stream_info(fmt_ctx, NULL);
	    	if (result < 0) {
	    		mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: could not find audio stream information (err. %d)", result);
	    	}
	    }

	    int stream_index = -1;
	    AVStream *st;
	    const AVCodec *dec = NULL;

	    if (result >= 0) {
	    	result = av_find_best_stream(fmt_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, NULL, 0);
		    if (result < 0) {
		    	mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: could not find %s stream in input file (err. %d)",
		    		                av_get_media_type_string(AVMEDIA_TYPE_AUDIO), result);
		    }
	    }

	    if (result >= 0) {
		    stream_index = result;
		    st = fmt_ctx->streams[stream_index];

		    /* find decoder for the stream */
		    dec = avcodec_find_decoder(st->codecpar->codec_id);

			if (!dec) {
				mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: failed to find %s codec",
										av_get_media_type_string(AVMEDIA_TYPE_AUDIO));
				result = -1;// or an AVERROR
			}
	    }

		/* Allocate a codec context for the decoder */
	    if (result >= 0) {
			audio_dec_ctx = avcodec_alloc_context3(dec);
			if (!audio_dec_ctx) {
				mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: failed to allocate the %s codec context",
										av_get_media_type_string(AVMEDIA_TYPE_AUDIO));
				result = -1;// or an AVERROR
			}
	    }

		/* Copy codec parameters from input stream to output codec context */
	    if (result >= 0) {
	    	result = avcodec_parameters_to_context(audio_dec_ctx, st->codecpar);
			if (result < 0) {
				mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: failed to copy the %s codec parameters to the decoder context (err. %d)",
						av_get_media_type_string(AVMEDIA_TYPE_AUDIO), result);
			}
	    }

		/* Init the decoder */
	    if (result >= 0) {
	    	result = avcodec_open2(audio_dec_ctx, dec, NULL);
			if (result < 0) {
				mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: failed to open the %s codec (err. %d)",
						av_get_media_type_string(AVMEDIA_TYPE_AUDIO), result);
			}
	    }
		audio_stream_idx = stream_index;
		// get the audio stream
		if (audio_stream_idx >= 0) {
			audio_stream = fmt_ctx->streams[audio_stream_idx];
			if (!audio_stream) {
				mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: the audio stream at index %d is null",
						audio_stream_idx);
				result = -1;
			}
		}

		AVFrame *frame = NULL;
		AVPacket *pkt = NULL;
		int au_buffer_size = 0;
		int au_buffer_nb_samples = 0;
		int au_buffer_duration = 0;
		// read a packet and retrieve information?
		if (result >= 0) {
		    frame = av_frame_alloc();
		    if (!frame) {
		    	mjlogWE(env, "N_FFAudioExtractor_initNativeFF: could not allocate an AVFrame (out of memory?)");
		    	result = -1;
		    }
		}

		if (result >= 0) {
		    pkt = av_packet_alloc();
		    if (!pkt) {
		    	mjlogWE(env, "N_FFAudioExtractor_initNativeFF: could not allocate an AVPacket (out of memory?)");
		    	result = -1;
		    } else {
		        /* read frames from the file */
		    	bool frame_processed = false;
		    	int nb_frames_pkt = 0;
		        while (av_read_frame(fmt_ctx, pkt) >= 0) {
	            	nb_frames_pkt++;
		            // check if the packet belongs to an audio stream
		            if (pkt->stream_index == audio_stream_idx) {
		            	// send the packet to the decoder
		            	int ret = avcodec_send_packet(audio_dec_ctx, pkt);
		                if (ret >= 0) {
		                	mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: packet size: %d, packet duration: %lld",
		                			pkt->size, pkt->duration);
		                	au_buffer_duration = pkt->duration;
		                    // receive the frames from the decoder
		                    while (ret >= 0) {
		                        ret = avcodec_receive_frame(audio_dec_ctx, frame);
		                        if (ret >= 0) {
		                        	au_buffer_nb_samples += (frame->nb_samples * frame->channels);

		                        	frame_processed = true;
		                        }
		                    }
		                } else {
		    		    	mjlogWE(env, "N_FFAudioExtractor_initNativeFF: error while sending a packet to the decoder");
		    		    	// stop or try another frame?
		                	//result = -1;
		                }
		                av_packet_unref(pkt);
		            }
	                if (nb_frames_pkt > 10 || frame_processed) {
	                	mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: number of frames read from packet: %d", nb_frames_pkt);
	                	break;
	                }
		        }

		        if (!frame_processed) {
		        	mjlogWE(env, "N_FFAudioExtractor_initNativeFF: failed to receive frames from the decoder");
		        	result = -1;
		        }
		        if (au_buffer_nb_samples > 0) {
		        	au_buffer_size = au_buffer_nb_samples * av_get_bytes_per_sample(audio_dec_ctx->sample_fmt);
		        	mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: audio buffer number of samples: %d, buffer size: %d, buffer duration %.4f sec",
		        			au_buffer_nb_samples, au_buffer_size, (au_buffer_nb_samples / (double)audio_dec_ctx->sample_rate));
		        } else {
		        	au_buffer_size = 1024;
		        }
		    }
		}

		// get information from decoder and frame

		if (result >= 0) {
	        enum AVSampleFormat sfmt = audio_dec_ctx->sample_fmt;
	        int n_channels = audio_dec_ctx->channels;
	        const char *fmt = av_get_sample_fmt_name(sfmt);
	        int bps = av_get_bytes_per_sample(sfmt);
	        const char *packed;
	        int is_planar;
	        if (av_sample_fmt_is_planar(sfmt)) {
	        	packed = "planar";
	        	is_planar = 1;
//	        	mjlogfWE(env, "N_AudioExtractor: the sample format the decoder produced is planar (name: %s)",
//	                   packed ? packed : "?");
//	            sfmt = av_get_packed_sample_fmt(sfmt);
//	            n_channels = 1;
	        } else {
	        	packed = "interleaved";
	        	is_planar = 0;
	        }

	        double dur_sec = 0;
	        AVRational avr = audio_dec_ctx->framerate;
	        dur_sec = av_q2d(avr);
	        int64_t dur_t = audio_stream->duration;
	        if (audio_stream->time_base.den != 0) {
	        	dur_sec = (dur_t * audio_stream->time_base.num) / (double)audio_stream->time_base.den;
	        	if (ff_debug) {
	        		mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: stream time base numerator: %d, denominator: %d",
	        			audio_stream->time_base.num, audio_stream->time_base.den);
	        	}
	        }

	        mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: the decoded audio info: name: %s, packaging: %s", fmt, packed);
	        mjlogfWE(env, "\t#channels: %d, stream duration: %lld, duration sec: %.3f", n_channels, dur_t, dur_sec);
	    	mjlogfWE(env, "\tsample rate: %d, bit rate: %lld, bytes per sample: %d", audio_dec_ctx->sample_rate,
	    	        		 audio_dec_ctx->bit_rate, bps);
	    	const AVInputFormat *input_format = fmt_ctx->iformat;
	    	mjlogfWE(env, "\tinput format name: %s", input_format->name);

	    	fmt_ctx->seek2any = 1;//??
	    	//create the struct to work with
	    	FFAudioExtractor *ffae = (FFAudioExtractor*) av_mallocz(sizeof(*ffae));
	    	if (ffae) {
	    		ffae->audio_dec_ctx = audio_dec_ctx;
	    		ffae->audio_stream = audio_stream;
	    		ffae->audio_stream_idx = audio_stream_idx;
	    		ffae->format_ctx = fmt_ctx;
	    		ffae->input_format = fmt_ctx->iformat;
	    		ffae->bytes_per_sample = bps;
	    		ffae->is_planar = is_planar;
	    		ffae->buffer_number_samples = au_buffer_nb_samples;
	    		ffae->sample_buffer_size = au_buffer_size;
	    		ffae->sample_buffer_duration = au_buffer_duration;
	    		ffae->jRef = (int64_t) env->NewGlobalRef(callerObj);
	    		if(sfmt == AV_SAMPLE_FMT_FLT || sfmt == AV_SAMPLE_FMT_FLTP ||
	    				sfmt == AV_SAMPLE_FMT_DBL || sfmt == AV_SAMPLE_FMT_DBLP) {
	    			ffae->format_tag = 3;// IEEE float
	    		}
	    		ae_id = (jlong) ffae;
	    	}
		} else {
        	mjlogfWE(env, "N_FFAudioExtractor_initNativeFF: unable to create an extractor for the file: %d",
        			result);
		}

	    //
	    env->ReleaseStringUTFChars(mediaPath, mediaURLChars);
	    //if (audio_dec_ctx)
	    //	avcodec_free_context(&audio_dec_ctx);
	    //if (fmt_ctx)
	    //	avformat_close_input(&fmt_ctx);
	    if (pkt)
	    	av_packet_free(&pkt);
	    if (frame)
	    	av_frame_free(&frame);

	    return ae_id;
	}


	/*
	 * Returns the sample frequency of the audio track.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    getSampleFrequencyFF
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_nl_mpi_media_FFAudioExtractor_getSampleFrequencyFF
	(JNIEnv *env, jobject callerObj, jlong objId) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;

		if (ffae) {
			if (ff_debug) {
				mjlogfWE(env, "N_FFAudioExtractor_getSampleFrequencyFF: the sample frequency: %d",
						(jint) ffae->audio_dec_ctx->sample_rate);
			}
			return (jint) ffae->audio_dec_ctx->sample_rate;
		}

		return 0;
	}

	/*
	 * Returns the bits per sample value of the audio.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    getBitsPerSampleFF
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_nl_mpi_media_FFAudioExtractor_getBitsPerSampleFF
	(JNIEnv *env, jobject callerObj, jlong objId) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;

		if (ffae) {
			if (ff_debug) {
				mjlogfWE(env, "N_FFAudioExtractor_getBitsPerSampleFF: the number of bits per sample: %d",
						(jint) (8 * av_get_bytes_per_sample(ffae->audio_dec_ctx->sample_fmt)));
			}
			return (jint) 8 * av_get_bytes_per_sample(ffae->audio_dec_ctx->sample_fmt);
		}

		return 0;
	}

	/*
	 * Returns the number of audio channels.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    getNumberOfChannelsFF
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_nl_mpi_media_FFAudioExtractor_getNumberOfChannelsFF
	(JNIEnv *env, jobject callerObj, jlong objId) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;

		if (ffae) {
			if (ff_debug) {
				mjlogfWE(env, "N_FFAudioExtractor_getNumberOfChannelsFF: the number of audio channels: %d",
						(jint) ffae->audio_dec_ctx->channels);
			}
			return (jint) ffae->audio_dec_ctx->channels;
		}

		return 0;
	}


	/*
	 * Returns the format of the produced data, 1 for PCM (uncompressed) integers,
	 * 3 for IEEE floats.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    getFormatTagFF
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_nl_mpi_media_FFAudioExtractor_getFormatTagFF
	(JNIEnv *env, jobject callerObj, jlong objId) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;

		if (ffae) {
			if (ff_debug) {
				mjlogfWE(env, "N_FFAudioExtractor_getFormatTagFF: the format tag of the produced data: %d",
						(jint) ffae->format_tag);
			}
			return (jint) ffae->format_tag;
		}

		return 1;
	}

	/*
	 * Returns the media duration in milliseconds. 
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    getDurationMsFF
	 * Signature: (J)J
	 */
	JNIEXPORT jlong JNICALL Java_nl_mpi_media_FFAudioExtractor_getDurationMsFF
	(JNIEnv *env, jobject callerObj, jlong objId) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;

		if (ffae) {
	        double dur_sec = 0;
	        int64_t dur_t = ffae->audio_stream->duration;
	        if (ffae->audio_stream->time_base.den != 0) {
	        	dur_sec = (dur_t * ffae->audio_stream->time_base.num) / (double)ffae->audio_stream->time_base.den;
	        	if (ff_debug) {
					mjlogfWE(env, "N_FFAudioExtractor_getDurationMsFF: stream duration in ms.: %lld",
							(jlong) (1000 * dur_sec));
	        	}
	        	return (jlong) (1000 * dur_sec);
	        } else {
	        	if (ff_debug) {
	        		mjlogWE(env, "N_FFAudioExtractor_getDurationMsFF: unknown stream duration, the time base denominator is 0");
	        	}
	        }
		}

		return 0;
	}

	/*
	 * Returns the duration in seconds.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    getDurationSecFF
	 * Signature: (J)D
	 */
	JNIEXPORT jdouble JNICALL Java_nl_mpi_media_FFAudioExtractor_getDurationSecFF
	(JNIEnv *env, jobject callerObj, jlong objId) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;

		if (ffae) {
	        double dur_sec = 0;
	        int64_t dur_t = ffae->audio_stream->duration;
	        if (ffae->audio_stream->time_base.den != 0) {
	        	dur_sec = (dur_t * ffae->audio_stream->time_base.num) /
	        				(double)ffae->audio_stream->time_base.den;
	        	if (ff_debug) {
					mjlogfWE(env, "N_FFAudioExtractor_getDurationSecFF: stream duration in sec.: %.3f",
							dur_sec);
	        	}
	        	return (jdouble) dur_sec;
	        } else {
	        	if (ff_debug) {
	        		mjlogWE(env, "N_FFAudioExtractor_getDurationSecFF: unknown stream duration, the time base denominator is 0");
	        	}
	        }
		}

		return 0.0;
	}

	/*
	 * Returns the size in bytes of the buffer the Media Foundation uses for a single read and decode action.
	 * The size may depend on the format and compression of the audio.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    getSampleBufferSizeFF
	 * Signature: (J)J
	 */
	JNIEXPORT jlong JNICALL Java_nl_mpi_media_FFAudioExtractor_getSampleBufferSizeFF
	(JNIEnv *env, jobject callerObj, jlong objId) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;

		if (ffae) {
			if (ff_debug) {
				mjlogfWE(env, "N_FFAudioExtractor_getSampleBufferSizeFF: sample buffer size in bytes: %lld",
						ffae->sample_buffer_size);
			}

	        return (jlong) ffae->sample_buffer_size;
		}

		return 0;
	}

	/*
	 * Returns the duration of a single buffer in milliseconds.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    getSampleBufferDurationMsFF
	 * Signature: (J)J
	 */
	JNIEXPORT jlong JNICALL Java_nl_mpi_media_FFAudioExtractor_getSampleBufferDurationMsFF
	(JNIEnv *env, jobject callerObject, jlong objId) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;

		if (ffae) {
			if (ffae->audio_dec_ctx->sample_rate > 0) {
				if (ff_debug) {
					mjlogfWE(env, "N_FFAudioExtractor_getSampleBufferDurationMsFF: buffer duration ms: %lld",
						(jlong) 1000 * (ffae->buffer_number_samples /
								(jdouble) ffae->audio_dec_ctx->sample_rate));
				}
				return (jlong) 1000 * (ffae->buffer_number_samples /
							(jdouble) ffae->audio_dec_ctx->sample_rate);
			} else {
				if (ff_debug) {
					mjlogfWE(env, "N_FFAudioExtractor_getSampleBufferDurationMsFF: unknown buffer duration ms, the sample rate is 0");
				}
			}
		}

		return 0;
	}

	/*
	 * Returns the duration of a single buffer in seconds.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    getSampleBufferDurationSecFF
	 * Signature: (J)D
	 */
	JNIEXPORT jdouble JNICALL Java_nl_mpi_media_FFAudioExtractor_getSampleBufferDurationSecFF
	(JNIEnv *env, jobject callerObject, jlong objId) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;

		if (ffae) {
			if (ffae->audio_dec_ctx->sample_rate > 0) {
				if (ff_debug) {
					mjlogfWE(env, "N_FFAudioExtractor_getSampleBufferDurationSecFF: buffer duration: %.3f",
							(ffae->buffer_number_samples / (jdouble) ffae->audio_dec_ctx->sample_rate));
				}
				return (jdouble) (ffae->buffer_number_samples /
							(jdouble) ffae->audio_dec_ctx->sample_rate);
			} else {
				if (ff_debug) {
					mjlogfWE(env, "N_FFAudioExtractor_getSampleBufferDurationSecFF: unknown duration, the sample rate is 0");
				}
			}
		}
		
		return 0.0;
	}

	/*
	 * Retrieves the decoded samples for the specified time span, copies them into the provided ByteBuffer
	 * and returns the number of copied bytes.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    getSamplesFF
	 * Signature: (JDDLjava/nio/ByteBuffer;)I
	 */
	JNIEXPORT jint JNICALL Java_nl_mpi_media_FFAudioExtractor_getSamplesFF
	(JNIEnv *env, jobject callerObj, jlong objId, jdouble fromTime, jdouble toTime, jobject byteBuffer) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;
		int num_copied = 0;
		AVFrame *frame = NULL;
		AVPacket *pkt = NULL;
		if (ffae) {
			uint8_t *buf_address = (uint8_t *) env->GetDirectBufferAddress(byteBuffer);
			jlong buf_length = (jlong) env->GetDirectBufferCapacity(byteBuffer);
			if (ff_debug) {
				mjlogfWE(env, "N_FFAudioExtractor_getSamplesFF: from: %.3f, to: %.3f, buffer size: %lld",
						fromTime, toTime, buf_length);
			}
			// convert the time stamps to decoder time_base units
			int64_t orig_from_ts = (int64_t)((fromTime * ffae->audio_dec_ctx->time_base.den) /
					ffae->audio_dec_ctx->time_base.num);
			int64_t rep_from_ts = orig_from_ts;
			if (ff_debug) {
				mjlogfWE(env, "N_FFAudioExtractor_getSamplesFF: original seek to: %lld", orig_from_ts);
			}

			if (rep_from_ts > (5 * ffae->sample_buffer_duration)) {
				rep_from_ts -= (5 * ffae->sample_buffer_duration);
			} else {
				rep_from_ts = 0;
			}
			//if from_ts >= sample buffer/packet duration, subtract that duration to start a bit before
			int64_t to_ts = (int64_t)((toTime * ffae->audio_dec_ctx->time_base.den) /
					ffae->audio_dec_ctx->time_base.num);
			int64_t frame_dur_ts = 0;//calculate based on num_samples and frame rate
			if (ff_debug) {
				mjlogfWE(env, "N_FFAudioExtractor_getSamplesFF: requested seek to: %lld, read seek to: %lld, stream index: %d",
					orig_from_ts, rep_from_ts, ffae->audio_stream_idx);
			}
			int result = av_seek_frame(ffae->format_ctx, ffae->audio_stream_idx, rep_from_ts, AVSEEK_FLAG_ANY);

			if (result >= 0) {
				// start reading
			    frame = av_frame_alloc();
			    if (!frame) {
			    	return num_copied;
			    }

				pkt = av_packet_alloc();
				if (!pkt) {
					return num_copied;
				}

				while (av_read_frame(ffae->format_ctx, pkt) >= 0) {
					if (pkt->stream_index == ffae->audio_stream_idx) {
						if (pkt->pts + pkt->duration <= orig_from_ts) {
							if (ff_debug) {
								mjlogfWE(env, "N_FFAudioExtractor_getSamplesFF: AVPacket before requested seek to, pts: %lld, duration: %lld",
									pkt->pts, pkt->duration);
							}
							av_packet_unref(pkt);
							continue;
						}
						int ret = avcodec_send_packet(ffae->audio_dec_ctx, pkt);
		                if (ret >= 0) {
		                    // receive the frames from the decoder
		                    while (ret >= 0 && num_copied < buf_length) {
		                        ret = avcodec_receive_frame(ffae->audio_dec_ctx, frame);
		                        if (ret >= 0) {
		                        	//mjlogfWE(env, "N_AudioExtractor: getSamples: frame format: %d, name: %s",
		                        		//	frame->format, av_get_sample_fmt_name((AVSampleFormat)(frame->format)));
		                        	// copy data from one or two channels to the byte buffer
		                        	// check frame pts + duration based on number of samples
		                        	if (frame_dur_ts == 0) {
		                        		double frame_dur_sec = frame->nb_samples / (double) frame->sample_rate;
		                        		frame_dur_ts = (int64_t) ((frame_dur_sec * ffae->audio_dec_ctx->time_base.den) /
		                        				ffae->audio_dec_ctx->time_base.num);
		                        	}
		                        	// if a frame is entirely before the requested from time, skip it,
		                        	// but process partially overlapping frames (from time before requested
		                        	// start time, end time after requested from time
		                        	if (frame->pts + frame_dur_ts < orig_from_ts) {
		                        		/*
		                        		if (frame->pts + frame_dur_ts < from_ts) {
		                        			//continue;
		                        		} else {
		                        			// copy part of the frame's samples
		                        		}
		                        		*/
		                        		av_frame_unref(frame);
		                        		continue;
		                        	}
		                        	//
		                        	if (num_copied == 0 && ff_debug) {
		                        		mjlogfWE(env, "N_FFAudioExtractor_getSamplesFF: frame presentation time: %lld",
		                        				                        			frame->pts);
		                        	}
		                        	//else
		                        	if (frame->pts > to_ts) {
		                        		av_frame_unref(frame);
		                        		break;// or read until the buffer is full?
		                        	}
		                        	size_t unpadded_linesize = frame->nb_samples * ffae->bytes_per_sample;
		                        	if (ffae->is_planar && frame->channels > 1) {
		                        		// iterate over extended_data and then over channels or planes,
		                        		// turns planar into interleaved, maybe not the best option in terms of performance
		                        		for (unsigned int i = 0; i < unpadded_linesize - ffae->bytes_per_sample; i += ffae->bytes_per_sample) {
		                        			for (int j = 0; j < frame->channels && j < 2; j++) {
		                        				memcpy(buf_address + num_copied, frame->extended_data[j] + i, ffae->bytes_per_sample);
		                        				num_copied += ffae->bytes_per_sample;
		                        				if (buf_length - num_copied < ffae->bytes_per_sample) break;
		                        			}
		                        			if (buf_length - num_copied < ffae->bytes_per_sample) break;
		                        		}
		                        	} else {
										// normal copying of data
										for(int i = 0; i < frame->channels && i < 2; i++) {
											if (frame->extended_data && frame->extended_data[i]) {
												size_t remain = (size_t) (buf_length - num_copied);
												size_t to_copy = unpadded_linesize <= remain ? unpadded_linesize : remain;
												memcpy(buf_address + num_copied, frame->extended_data[i], to_copy);
												num_copied += to_copy;
											}
										}
		                        	}
		                        } else {
		                        	if ((ret == AVERROR_EOF || ret == AVERROR(EAGAIN))) {
		                        		if (ff_debug) {
		                        			mjlogWE(env, "N_FFAudioExtractor_getSamplesFF: receive frame failed, probably end of file or end of buffer");
		                        		}
		                        	}
		                        }
		                    }
		                    av_frame_unref(frame);
		                }
					}
					av_packet_unref(pkt);
				}
			} else {
				// log failure of seeking
			}

		}
		/*
		if (frame)
			av_frame_free(&frame);
		if (pkt)
			av_packet_free(&pkt);
		*/
		avcodec_flush_buffers(ffae->audio_dec_ctx);
		if (ff_debug) {
			mjlogfWE(env, "N_FFAudioExtractor_getSamplesFF: number of copied bytes: %d", num_copied);
		}
		return num_copied;
	}

	/*
	 * Retrieves a sample and copies it to the ByteBuffer. In practice the decoder fills a native
	 * buffer which usually is much larger than the number of bytes required for a single sample.
	 * There is no guarantee that the requested sample is in the first or last bytes of the buffer.
	 * Returns the number of copied bytes.
	 *
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    getSampleFF
	 * Signature: (JDLjava/nio/ByteBuffer;)I
	 */
	JNIEXPORT jint JNICALL Java_nl_mpi_media_FFAudioExtractor_getSampleFF
	(JNIEnv *env, jobject callerObj, jlong objId, jdouble fromTime, jobject byteBuffer) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;
		int num_copied = 0;
		AVFrame *frame = NULL;
		AVPacket *pkt = NULL;

		if (ffae) {
			uint8_t *buf_address = (uint8_t *) env->GetDirectBufferAddress(byteBuffer);
			jlong buf_length = (jlong) env->GetDirectBufferCapacity(byteBuffer);
			int64_t orig_from_ts = (int64_t)((fromTime * ffae->audio_dec_ctx->time_base.den) /
					ffae->audio_dec_ctx->time_base.num);
			int64_t rep_from_ts = orig_from_ts;// reposition the  time stamp
			if (rep_from_ts > (5 * ffae->sample_buffer_duration)) {
				rep_from_ts -= (5 * ffae->sample_buffer_duration);
			} else {
				rep_from_ts = 0;
			}
			int64_t frame_dur_ts = 0;//calculate based on num_samples and framerate
			int result = av_seek_frame(ffae->format_ctx, ffae->audio_stream_idx, rep_from_ts, AVSEEK_FLAG_ANY);

			if (result >= 0) {
				// start reading
			    frame = av_frame_alloc();
			    if (!frame) {
			    	if (ff_debug) {
			    		mjlogWE(env, "N_FFAudioExtractor_getSampleFF: could not allocate frame, out of memory");
			    	}
			    	return num_copied;
			    }

				pkt = av_packet_alloc();
				if (!pkt) {
					if (ff_debug) {
						mjlogWE(env, "N_FFAudioExtractor_getSampleFF: could not allocate packet, out of memory");
			    	}
					return num_copied;
				}

				while (av_read_frame(ffae->format_ctx, pkt) >= 0) {
					if (pkt->stream_index == ffae->audio_stream_idx) {
						if (pkt->pts + pkt->duration <= orig_from_ts) {
							if (ff_debug) {
								mjlogfWE(env, "N_FFAudioExtractor_getSampleFF: AVPacket before requested seek time, pts: %lld, duration: %lld",
									pkt->pts, pkt->duration);
							}
							av_packet_unref(pkt);
							continue;
						}
						if (pkt->pts > orig_from_ts) {
							if (ff_debug) {
								mjlogfWE(env, "N_FFAudioExtractor_getSampleFF: AVPacket before requested seek time, pts: %lld, duration: %lld",
									pkt->pts, pkt->duration);
							}
							av_packet_unref(pkt);
							break;
						}
						int ret = avcodec_send_packet(ffae->audio_dec_ctx, pkt);
		                if (ret >= 0) {
		                    // receive the frames from the decoder
		                    while (ret >= 0 && num_copied < buf_length) {
		                        ret = avcodec_receive_frame(ffae->audio_dec_ctx, frame);
		                        if (ret >= 0) {
		                        	//mjlogfWE(env, "N_AudioExtractor: getSamples: frame format: %d, name: %s",
		                        		//	frame->format, av_get_sample_fmt_name((AVSampleFormat)(frame->format)));
		                        	// copy data from one or two channels to the byte buffer
		                        	// check frame pts + duration based on number of samples
		                        	if (frame_dur_ts == 0) {
		                        		double frame_dur_sec = frame->nb_samples / (double) frame->sample_rate;
		                        		frame_dur_ts = (int64_t) ((frame_dur_sec * ffae->audio_dec_ctx->time_base.den) /
		                        				ffae->audio_dec_ctx->time_base.num);
		                        	}
		                        	// if a frame is entirely before the requested from time, skip it,
		                        	// but process partially overlapping frames (from time before requested
		                        	// start time, end time after requested from time
		                        	if (frame->pts + frame_dur_ts < orig_from_ts) {
		                        		av_frame_unref(frame);
		                        		continue;
		                        	}
		                        	//
		                        	if (num_copied == 0 && ff_debug) {
		                        		mjlogfWE(env, "N_FFAudioExtractor_getSampleFF: frame presentation time: %lld",
		                        				                        			frame->pts);
		                        	}

		                        	size_t unpadded_linesize = frame->nb_samples * ffae->bytes_per_sample;
		                        	if (ffae->is_planar && frame->channels > 1) {
		                        		// iterate over extended_data and then over channels or planes,
		                        		// turns planar into interleaved, maybe not the best option in terms of performance
		                        		for (unsigned int i = 0; i < unpadded_linesize - ffae->bytes_per_sample; i += ffae->bytes_per_sample) {
		                        			for (int j = 0; j < frame->channels && j < 2; j++) {
		                        				memcpy(buf_address + num_copied, frame->extended_data[j] + i, ffae->bytes_per_sample);
		                        				num_copied += ffae->bytes_per_sample;
		                        				if (buf_length - num_copied < ffae->bytes_per_sample) break;
		                        			}
		                        			if (buf_length - num_copied < ffae->bytes_per_sample) break;
		                        		}
		                        	} else {
										// normal copying of data
										for(int i = 0; i < frame->channels && i < 2; i++) {
											if (frame->extended_data[i]) {
												size_t remain = (size_t) (buf_length - num_copied);
												size_t to_copy = unpadded_linesize <= remain ? unpadded_linesize : remain;
												memcpy(buf_address + num_copied, frame->extended_data[i], to_copy);
												num_copied += to_copy;
											}
										}
		                        	}
		                        }
		                    }
		                    av_frame_unref(frame);
		                }
		                // if we get here, one packet containing the requested time has been processed
		                av_packet_unref(pkt);
		                break;
					} // end if (pkt->stream_index == ffae->audio_stream_idx)
					av_packet_unref(pkt);
				} // end while (av_read_frame(ffae->format_ctx, pkt) >= 0)
			} // end if (av_seek_frame(...)) succeeded
		}// end if(ffae)

		/*
		if (frame)
			av_frame_free(&frame);
		if (pkt)
			av_packet_free(&pkt);
		*/
		avcodec_flush_buffers(ffae->audio_dec_ctx);
		if (ff_debug) {
			mjlogfWE(env, "N_FFAudioExtractor_getSampleFF: number of copied bytes: %d", num_copied);
		}
		return num_copied;
	}

	/*
	 * Not really implemented, seeking is part of each sample reading operation.
	 * Returns true if there is an extractor structure, false otherwise.
	 *
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    setPositionSecFF
	 * Signature: (JD)Z
	 */
	JNIEXPORT jboolean JNICALL Java_nl_mpi_media_FFAudioExtractor_setPositionSecFF
	(JNIEnv *env, jobject callerObject, jlong objId, jdouble seekTime) {
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;

		if (ffae) {
			if (ff_debug) {
				mjlogWE(env, "N_FFAudioExtractor_setPositionSecFF: not implemented");
			}
			return JNI_TRUE;
		}
		
		return JNI_FALSE;
	}

	/*
	 * Deletes the global reference and the MMFAudioExtractor instance.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    releaseFF
	 * Signature: (J)V
	 */
	JNIEXPORT void JNICALL Java_nl_mpi_media_FFAudioExtractor_releaseFF
	(JNIEnv *env, jobject callerObj, jlong objId) {
		// delete global ref etc
		FFAudioExtractor *ffae = (FFAudioExtractor*) objId;

		if (ffae) {
		    if (ffae->audio_dec_ctx) {
		    	avcodec_flush_buffers(ffae->audio_dec_ctx);
		    	avcodec_free_context(&ffae->audio_dec_ctx);
		    }
		    if (ffae->format_ctx) {
		    	avformat_close_input(&ffae->format_ctx);
		    }
			if (ff_debug) {
				mjlogfWE(env, "N_FFAudioExtractor_releaseFF: releasing extractor id: %lld", ffae);
			}
		    env->DeleteGlobalRef((jobject)(ffae->jRef));
		    delete ffae;
		}
	}

	/*
	 * Enables or disables debug mode.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    setDebugMode
	 * Signature: (Z)V
	 */
	JNIEXPORT void JNICALL Java_nl_mpi_media_FFAudioExtractor_setDebugMode
	(JNIEnv *env, jclass callerClass, jboolean debugMode) {
		ff_debug = (int) debugMode;
	}

	/*
	 * Returns whether the debug mode is on or off.
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    isDebugMode
	 * Signature: ()Z
	 */
	JNIEXPORT jboolean JNICALL Java_nl_mpi_media_FFAudioExtractor_isDebugMode
	(JNIEnv *env, jclass callerClass) {
		return (jboolean) ff_debug;
	}

	/*
	 * Class:     nl_mpi_media_FFAudioExtractor
	 * Method:    initLog
	 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
	 */
	JNIEXPORT void JNICALL Java_nl_mpi_media_FFAudioExtractor_initLog
	(JNIEnv *env, jclass callerClass, jstring clDescriptor, jstring methodName) {
		const char *clChars = env->GetStringUTFChars(clDescriptor, NULL);
		if (clChars == NULL) {
			return; // OutOfMemory thrown
		}
		const char *methChars = env->GetStringUTFChars(methodName, NULL);
		if (methChars == NULL) {
			return; // OutOfMemory thrown
		}
		mpijni_initLog(env, clChars, methChars);
		if (ff_debug) {
			mjlogfWE(env, "N_AudioExtractor.initLogFF: set Java log method: %s - %s", clChars, methChars);
		}

		// free the allocated memory
		env->ReleaseStringUTFChars(clDescriptor, clChars);
		env->ReleaseStringUTFChars(methodName, methChars);
	}
/*
int main(int argc, char **argv)
{
return 0;
}
*/

#ifdef __cplusplus
}
#endif
