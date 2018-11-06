class Oscilloscope {
  constructor (source, options = {}) {
	if (!(source instanceof window.AudioNode)) {
	  throw new Error('Oscilloscope source must be an AudioNode')
	}

	if (source instanceof window.AnalyserNode) {
	  this.analyser = source
	} else {
	  this.analyser = source.context.createAnalyser()
	  source.connect(this.analyser)
	}

	if (options.fftSize) { this.analyser.fftSize = options.fftSize }
	this.timeDomain = new Uint8Array(this.analyser.frequencyBinCount)
	this.drawRequest = 0
	this.drawfns = {"sine": this.draw.bind(this), 
					"spectrum": this.drawFrequencySpectrum.bind(this)}
	this.drawfn = this.drawfns["sine"]
  }

  setDrawfn(name) {
  	this.drawfn = this.drawfns[name]
  }

  // begin default signal animation
  animate (ctx, x0, y0, width, height) {
	if (this.drawRequest) {
	  throw new Error('Oscilloscope animation is already running')
	}
	this.ctx = ctx
	this.gradient = ctx.createLinearGradient(0, 0, 0, ctx.canvas.height)
	this.gradient.addColorStop(1, '#000000')
    this.gradient.addColorStop(0.75, '#14fdce')
    this.gradient.addColorStop(0.25, '#0B5941')
    this.gradient.addColorStop(0, '#ffffff')
 
	const drawLoop = () => {
	  ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
	 
	  if (this.drawfn) {
	   	this.drawfn(ctx, x0, y0, width, height)
	  }
	  this.drawRequest = window.requestAnimationFrame(drawLoop)
	}
	drawLoop()
  }

  // stop default signal animation
  stop () {
	if (this.drawRequest) {
	  window.cancelAnimationFrame(this.drawRequest)
	  this.drawRequest = 0
	  this.ctx.clearRect(0, 0, this.ctx.canvas.width, this.ctx.canvas.height)
	}
  }

  drawLines (ctx, width, height) {

	ctx.beginPath()
	let nLines = 5
	let xStep = width / nLines
	let yStep = height / nLines

	for (let x = 0; x < width; x+=xStep) {
		ctx.moveTo (x, 0)
		ctx.lineTo (x, height)
	}

	for (let y = 0; y < height; y+=yStep) {
		ctx.moveTo (0, y)
		ctx.lineTo(width, y)
	}
	let color = ctx.strokeStyle
	ctx.strokeStyle = "#0B5941"
	ctx.stroke()
	ctx.strokeStyle = color
  }

  // draw signal
  draw (ctx, x0 = 0, y0 = 0, width = ctx.canvas.width - x0, height = ctx.canvas.height - y0) {
	this.analyser.getByteTimeDomainData(this.timeDomain)
	const step = width / this.timeDomain.length
	this.drawLines(ctx, width, height)
	ctx.beginPath()
	// drawing loop (skipping every second record)
	for (let i = 0; i < this.timeDomain.length; i += 2) {
	  const percent = this.timeDomain[i] / 256
	  const x = x0 + (i * step)
	  const y = y0 + (height * percent)
	  ctx.lineTo(x, y)
	}

	ctx.stroke()
  }

  drawFrequencySpectrum (ctx, x0 = 0, y0 = 0, width = ctx.canvas.width - x0, height = ctx.canvas.height - y0) {
	let array =  new Uint8Array(this.analyser.frequencyBinCount)
	this.analyser.getByteFrequencyData(array)
	ctx.fillStyle = this.gradient
	this.drawSpectrum(ctx, array, width, height)
  }
  drawSpectrum(ctx, array, width, height) {
	for (let i = 0; i < array.length; i++) {
		let value = array[i];
		ctx.fillRect(i * 5, height - value, 3, height)
	}
  } 
}

window.Oscilloscope = Oscilloscope;

