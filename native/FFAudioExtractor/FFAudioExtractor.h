#ifndef FF_AUDIO_EXTRACTOR_H
#define FF_AUDIO_EXTRACTOR_H


#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>

static int ff_debug = 0;

typedef struct FFAudioExtractor{
	AVFormatContext     *format_ctx;
    AVCodecContext      *audio_dec_ctx;
    int                  audio_stream_idx = -1;
    AVStream            *audio_stream;
    const AVInputFormat *input_format;
    int                  bytes_per_sample;
    int                  is_planar = 0;

    // for estimation of byte buffer size, a multiple of sample buffer size
    int                  buffer_number_samples;
    int                  sample_buffer_size;
    int64_t              sample_buffer_duration;// sample buffer duration in AVStream time-base units
    int                  format_tag = 1;

    int64_t              jRef;
} FFAudioExtractor;

#endif /* FF_AUDIO_EXTRACTOR_H */
