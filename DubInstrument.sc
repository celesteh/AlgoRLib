DubInstrument {
	var <name, <>midi, dub, <alias, <pbind, pdef, <>downbeat= -9, <>upbeat= -12, <>offbeat = -13, <>rest= \rest, isplay,
	<>beatfigures, <>restfigures, <>playf, <>stopf, loop;

	*new {|name, midi, dub, alias, pbind|
		^super.newCopyArgs(name, midi, dub, alias, pbind)
	}

	//pdef {
	//	^pdef
	//}

	pdef {
		pdef.isNil.if({
			pdef = Pdef(name);
		});
		^pdef
	}


	next {

	}

	prev {


	}

	last{
	}

	rand {

	}

	loop_{|lp|
		var val;
		loop = lp.collect({|tuple|
			tuple.last.isKindOf(Symbol).if({
				{
					val = Message(this, tuple.last).value;
				}.try;
				val.notNil.if({
					tuple = tuple.copy;
					//"val is %".format(val).postln;
					tuple[1] = val;
				});
			});
			tuple;
		});
		^loop;
	}

	loop {
		^loop;
	}

	generate {

	}


	play {|clock, quant ...args|

		var pd;

		playf.notNil.if({
			playf.value(*args)
		});

		pd = this.pdef();

		pd.notNil.if({

			clock.isKindOf(ExternalClock).if({
				isplay.isNil.if({ isplay = false });
				isplay.not.if({
					"external".postln;
					pd.playExt(clock, nil, quant);
					isplay = true;
				});
			} , {
				"tempo".postln;
				pd.isPlaying.not.if({
					pd.play(clock, nil, quant);
				});
			});
		})

	}

	stop {|...args|
		var pd;

		stopf.notNil.if({
			stopf.value(*args)
		});

		pd = this.pdef();
		pd.notNil.if({pd.stop();})
	}


	pause {
		var pd;

		pd = this.pdef();
		pd.notNil.if({pd.pause();})

	}



	isPlaying {

		var pd, ret;

		pd = pdef.notNil.if ({ pdef }, { Pdef(name) });
		pd.isNil.if ({ ret = false; },
			{
				ret = isplay;
				ret.isNil.if ({
					ret = pd.isPlaying;
				});
		});
		ret.isNil.if ({ ret = false; });

		^ret;

	}

	volume_ {|func|
		downbeat= func.value(downbeat);
		upbeat=func.value(upbeat);
		offbeat=func.value(offbeat);
	}

	vol_{|f| this.volume_(f) }

}


DubMidiDrum : DubInstrument {

	var note, offset, loops, index;


	* new {|name, midi, note, dub, alias, offset=false, beatfigures, resfigures|

		^super.new.init(name, midi, note, dub, alias, offset, beatfigures, resfigures)
	}



	init {|iname, midiout, inote, idub, ialias, ioffset, ibeatfigures, irestfigures|



		midi = this.pr_midiconnect(midiout);
		name = iname;
		note = inote;
		dub = idub;
		alias = ialias;
		offset = ioffset;
		rest = Rest();
		beatfigures = ibeatfigures;
		restfigures = irestfigures;


		pbind = Pbind(
			\type, \midi,
			\midiout, midi,
			\chan, (0..12),
			\midinote, note,
			\hasGate, false
		);

		offset.if({
			pbind = Pbindf(pbind,
				\timingOffset, Pwhite(0.001, 0.002)
			);
		});

		loops = [];
		index = -1;

		Pdef(name,
			Pbindf(
				pbind,
				[\dur, \db], Prout ({

					inf.do({
						(this.loop.size > 0).if({
							//"looping".postln;
							this.loop.do({|item|
								item.yield
							})
						} , {
							"resting".postln;
							[1, Rest()].yield;
						})
					})
				})//,
				//\foo, Pfunc({|e| e.postln; })
			)
		);



	}


	pr_midiconnect {|midiout|
		var mout;

		midiout.notNil.if ({
			(midiout.isKindOf(String) || midiout.isKindOf(Symbol)).if ({
				//this.connect(midiout);
				MIDIClient.externalDestinations.do({|m, i|
					//m.postln;
					m.name.postln;
					(m.name.containsi(midiout)).if({
						mout = MIDIOut(i);
						mout.connect(0);
						i .postln;
						mout.postln;
						//"found1".postln;
					});
				})
			}, {
				mout = midiout;
			});
		})
		^mout;
	}

	next {
		var lp;

		"next".postln;

		index = index +1;
		lp = loops[index];

		//loops[index].postln;

		((index >= loops.size) || loops[index].isNil).if({
			//mel = generators[gindex].value(loop);
			//melodies = melodies.add(mel);
			this.generate;
		});

		//^melodies[index];
		^this.loop;
	}

	prev {
		index = index-1;
		(index < 0).if({
			index = 0;
			^this.loop;
		});
	}

	generate {
		//var mel;
		//"generate".postln;
		//mel = generators[gindex].value(loop);
		//melodies = melodies.add(mel);
		//loops = loops.add(dub.rythm);
		dub.makeBeats(name);
		^this.last();
	}

	last {
		index = loops.size -1;
		^this.loop();
	}

	rand {
		var oldindex;
		oldindex = index;
		{index == oldindex}.while({
			index = (loops.size +1).rand;
		});
		^this.loop();
	}


	loop {
		var lp, nils;

		lp = loops[index];

		((index >= loops.size) || lp.isNil).if({
			//^this.next()
			this.generate();
			//^this.last()

			// check for null
			((index < (loops.size -2)) && (index > 0)).if({
				"nil meldoy".postln;
				loops.removeAt(index);


				//cleanup
				nils = [];
				loops.do({|item, index|
					item.isNil.if({
						nils = nils.add(index);
					});
				});
				nils.reverse.do({|index|
					loops.removeAt(index)
				});

			});

			index = loops.size -1;
			^this.last;
		}, {
			^lp
		})
		^loops[index]
	}

	loop_ {|loop|
		var lp;

		lp = super.loop_(loop);
		loops = loops.add(lp);
		index = loops.size -1;
		this.loop.postln;
		^this.loop;
	}

	modify{|loop, ind|

		ind.isNil.if({ ind = index});
		loops[ind] = loop;
	}

	replaceAt{|item, ind|
		ind.isNil.if({ ind = index});
		loops[ind] = item;
	}

	/*
	play {|clock, quant|



	//clock.isKindOf(ExternalClock).if({
	//isplay.isNil.if({ isplay = false });
	//isplay.not.if({
	//"external".postln;
	//Pdef(name).playExt(clock, nil, quant);
	//isplay = true;
	//});
	//} , {
	//"tempo".postln;
	//Pdef(name).isPlaying.not.if({
	//Pdef(name).play(clock, nil, quant);
	//});
	//});

	super.play();


	}

	stop {
	Pdef(name).stop;
	}


	pause {
	Pdef(name).pause;
	}
	*/

}




DubMelody : DubInstrument {

	var <scale, melodies, index, generators, gindex, loop;

	*new{ |name, dub, scale|
		^super.new.init(name, dub, scale);
	}

	init {|name, idub, scale|

		rest = 0.ampdb;
		dub = idub;
		dub.add(name, this);
		this.ready(name, dub, scale);

	}

	ready{|nm, lp, scl|

		name = nm;

		"DubMelody.init".postln;
		lp.isKindOf(Dub).if({
			loop = lp.rythm;
			dub = lp;
		}, {
			loop = lp;
		});

		scale = scl;
		scale.isNil.if({
			scale = Scale.major;
		});

		melodies=[];
		index = -1;

		generators = [
			/*
			{|loop|
			var step, beats, degree, arr;
			step = 0;
			arr = [];
			{step < groove.steps}.while({
			beats = [1,1,1,1,2,2,3].choose;
			//step.postln;
			degree = 8.rand;
			arr =  arr++ [Step(degree, beats)];
			step = beats+step;
			});
			arr
			},*/
			{|loop|

				loop.collect({|item|
					//Dub.pr_isRest(item).if({
					//	[item.first, \rest, \rest, \rest]
					//} , {
					(item[1] == rest).if({
						item ++ [\rest, \rest]
					}, {
						item ++ [scale.degrees.rand, 2.rrand(4)]
					});
				})
			},
			{|loop|

				loop.collect({|item|
					//Dub.pr_isRest(item).if({
					//	[item.first, \rest, \rest, \rest]
					//} , {
					(item[1] == rest).if({
						item ++ [\rest, \rest]
					}, {
						item ++ [1.rrand(8), 2.rrand(4)]
					});
				})
			},

			{|loop|
				var arr, pyr, start;

				arr = melodies[0];

				arr.isNil.if({
					arr = generators[0].value(loop);
				});

				pyr = arr.pyramid(10.rand);
				start = (pyr.size - loop.size).rand;
				pyr[start..(start + loop.size)]
			}

		];

		gindex = 0;

	}

	melody {

		var mel, nils;
		mel = melodies[index];

		"index is %".format(index).postln;

		((index >= melodies.size) || mel.isNil).if({
			//^this.next()
			this.generate();
			//^this.last()

			// check for null
			((index < (melodies.size -2)) && (index > 0)).if({
				"nil meldoy".postln;
				melodies.removeAt(index);


				//cleanup
				nils = [];
				melodies.do({|item, index|
					item.isNil.if({
						nils = nils.add(index);
					});
				});
				nils.reverse.do({|index|
					melodies.removeAt(index)
				});

			});

			index = melodies.size -1;
			^melodies.last;
		}, {
			^mel
		})

	}

	next {
		var mel;

		"next".postln;

		index = index +1;
		//mel = melodies[index];

		//melodies[index].postln;

		//((index >= melodies.size) || melodies[index].isNil).if({
		//mel = generators[gindex].value(loop);
		//melodies = melodies.add(mel);
		//});

		//^melodies[index];
		^this.melody;
	}

	prev {
		index = index-1;
		(index < 0).if({
			index = 0;
			^this.melody;
		});
	}

	generate {|lp|
		var mel;

		lp.isNil.if({
			//lp = dub.rythm;
			dub.makeBeats(this);
		} , {
			super.loop_(lp);

			"generate".postln;
			//loop = dub.pr_rests(this, loop);

			mel = generators[gindex].value(loop);
			melodies = melodies.add(mel);
		});
	}

	last {
		index = melodies.size -1;
		^this.melody();
	}

	rand {
		var oldindex;
		oldindex = index;
		{index == oldindex}.while({
			index = (melodies.size +1).rand;
		});
		^this.melody();
	}

	loop_ {|lp|
		lp.isKindOf(Dub).if({
			loop = lp.rythm;
		}, {
			//loop = super.loop_(lp);

			loop = lp;
		});


		//this.next()
		this.generate(loop);
		^this.last()
	}

	add { |generatorFunction|
		generators = generators.add(generatorFunction);
		gindex = gindex +1;
	}

	update {|changed, changer|
		// we are only registered with the groove
		//"changer: % what: %".format(changer, what).postln;
		//(changer === groove && what == \rebeat).if({
		//	"new melody".postln;
		//	index = melodies.size -1;
		loop = changed;
		//this.next();
		//})
		this.generate();
		this.last();
	}

	/*
	ratio {|beat|
	var deg, mel;

	deg = (this.melody()[beat]);
	(deg == \rest).if({
	^deg
	});
	^scale.degreeToRatio(deg);
	}

	freq {|beat, rootFreq=440, octave=1|
	var deg, mel;
	deg = this.melody()[beat];
	(deg == \rest).if({
	^deg
	});
	^scale.degreeToFreq(deg, rootFreq, octave);
	}

	degree {|beat|
	^this.melody.wrapAt(beat)
	}
	*/

	pdef_{|pd|
		pdef = pd;
	}

	volume_{|func|
		super.volume_(func);
		melodies.do({|mel|
			mel = mel.collect({|tuple|
				tuple[1] = func.value(tuple[1]);
				tuple;
			})
		})
	}


}

DubLive : DubInstrument{

	var <>in, <numchannels, fx, bufs, pindex, rindex, <isRecording, semaphore;

	*new {|name, dub, in, numchannel|
		^super.new.init(name, dub, in, numchannel)
	}

	init{ |iname, idub, iin, inumchannels|

		name = iname;
		dub = idub;
		in = iin;
		numchannels = inumchannels;
		fx = dub.fx;
		rindex = 0;
		bufs = [ fx.makeBuffer(numchannels, name++rindex)];
		pindex = -1;
		isRecording = false;
		semaphore = Semaphore();
		pbind = Pbind(
			\bufnum, Pfunc({this.bufnum})
		);
	}

	record {|clock|
		clock.isKindOf(ExternalClock).if({
			clock = clock.tempoclock
		});
		{
			semaphore.wait;
			isRecording = true;
			bufs = bufs.add(fx.makeBuffer(numchannels, name++rindex));
			(instrument: \record++numchannels,
				bufnum: bufs[rindex],
				dur: clock.tempo * dub.loop,
				in: in
			).play(clock, [1,0]);
			clock.playNextBar({
				"recording".postln;
				{
					(clock.tempo * dub.loop).wait;
					clock.playNextBar({
						"stopped".postln;
						pindex = rindex;
						rindex = rindex + 1;
						isRecording = false;
						semaphore.signal;
					})
				}.fork;
			});
		}.fork;
	}

	next {
		pindex = (pindex+1).min(rindex -1);
		^this.bufnum;
	}

	prev {
		pindex = (pindex-1).max(0);
		^this.bufnum;
	}

	bufnum {
		^ bufs.clipAt(pindex).bufnum;
	}

	plot{
		bufs.clipAt(pindex).notNil.if({
			bufs.clipAt(pindex).plot
		});

	}

}