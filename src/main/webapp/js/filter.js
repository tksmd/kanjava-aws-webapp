(function() {

	var root = this;

	var Filter = function(g,src) {
		return new ConvolutionFilter(g,src);
	};

	root.Filter = Filter;
	Filter.VERSION = "0.0.1";

	var ConvolutionFilter = function(g,src) {
		this.g = g;
		this.src = src;
	};

	ConvolutionFilter.prototype = {
		blur : function() {
			return this.exec([ 1, 1, 1, 1, 1, 1, 1, 1, 1 ], 9, 0);
		},
		exec : function(kernel, divisor, bias) {
			var w = this.src.width, h = this.src.height, srcData = this.src.data, dstImage = this.g.createImageData(w,h), dstData = dstImage.data, r, g, b, i, j, k, step, kStep;
									
			for ( var y = 1; y < h - 1; y++) {
				step = y * w;
				for ( var x = 1; x < w - 1; x++) {
					r = g = b = 0;
					i = (step + x) << 2;
					k = 0;
					for ( var ky = -1; ky <= 1; ky++) {
						kStep = ky * w;
						for ( var kx = -1; kx <= 1; kx++) {
							j = (kStep << 2) + (kx << 2);
							r += srcData[i + j] * kernel[k];
							g += srcData[i + j + 1] * kernel[k];
							b += srcData[i + j + 2] * kernel[k];
							k++;
						}
					}
					dstData[i] = r / divisor + bias;
					dstData[i + 1] = g / divisor + bias;
					dstData[i + 2] = b / divisor + bias;
					
					if(dstData[i] + dstData[i+1] + dstData[i+2] < 255){
						dstData[i+3] = 0;
					}else{						
						dstData[i + 3] = 255;
					}					
				}
			}
			for ( var l = 0, len = dstData.length; l < len; l++) {
				var value = dstData[l];
				dstData[l] = value < 0 ? 0 : value > 255 ? 255 : value;
			}
			return dstImage;
		}
	};

})();