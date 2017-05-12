Dub {

	//var parts;
	var <clock, drums, seqs, /*<>downbeat= -9, <>upbeat= -12, <>offbeat = -13,*/ <loop=8, isPlaying, alias, restfigures, beatfigures, <fx, <>stopf;

	*new {|clock, midiout|
		^super.new.init(clock, midiout);
	}

	init {|clk, midiout|
		var kick, quavers;
		/*
		36 Kick Drum
		40 Snare Drum
		41 Lo Tom
		43 Hi Tom
		42 Cl. Hi Hat
		44 Cl. Hi Hat lang
		46 Open Hi Hat
		39 Clap
		37 Rim Shot
		49, 50 Crash
		52, 53 Ride
		*/

		quavers = Array(2);
		quavers = quavers.add([[[0.27, \downbeat], [0.24, \offbeat], [0.25, \offbeat], [0.24, \offbeat]]]);
		quavers = quavers.add([[[0.26, \upbeat], [0.245, \offbeat], [0.25, \offbeat], [0.245, \offbeat]]]);



		alias =(
			bd: \kick, kick: \kick,
			sd: \snare, snare: \snare,
			lt: \lotom, lotom: \lotom, lowtom: \lotom,
			ht: \hitom, hitom: \hitom, tom: \hitom, toms: \hitom,
			hhc: \clhat, hat:\clhat, clhat: \clhat,
			hhcl: \clhatl, clhatl: \clhatl, hatl: \clhatl, clhat: \clhatl,
			hho: \openhat, openhat: \openhat,
			cl:\clap, clap:  \clap,
			rs: \rim, rim: \rim, rimshot:\rim,
			crl: \locrash, cr: \locrash, locrash: \locrash, lowcrash: \locrash, crash: \locrash,
			crh: \hicrash, hcr: \hicrash, hicrash: \hicrash,
			rl: \loride, ridel:\loride, lr: \loride, lride: \loride, loride: \loride,
			rh: \hiride, rideh: \hiride, ride:\hiride, hride: \hiride, hiride: \hiride
		);

		kick = DubMidiDrum(\kick, midiout, 36, this, nil, false);

		drums = (
			kick: kick,
			snare: DubMidiDrum(\snare, kick.midi, 40, this, nil, false),
			lotom: DubMidiDrum(\lotom, kick.midi, 41, this, nil, false),
			hitom: DubMidiDrum(\hitom, kick.midi, 43, this, nil, false),
			clhat: DubMidiDrum(\clhat, kick.midi, 42, this, nil, true, quavers[0], quavers[1]),
			clhatl: DubMidiDrum(\clhatl, kick.midi, 44, this, nil, false),
			openhat: DubMidiDrum(\openhat, kick.midi, 46, this, nil, false),
			clap:  DubMidiDrum(\clap, kick.midi, 39, this, nil, true),
			rim: DubMidiDrum(\rim, kick.midi, 37, this, nil, true),
			locrash: DubMidiDrum(\locrash, kick.midi, 49, this, nil, true, [[[loop, \downbeat]]],[[[loop, \downbeat]]]),
			hicrash: DubMidiDrum(\hicrash, kick.midi, 50, this, nil, true, [[[[loop, \downbeat]]], [[[loop, \downbeat]]]]),
			loride: DubMidiDrum(\loride, kick.midi, 52, this, nil, true),
			hiride: DubMidiDrum(\hiride, kick.midi, 53, this, nil, true)
		);

		drums.know=true;

		//seqs = IdentityDictionary();
		//seqs.know=true;

		//isPlaying = IdentityDictionary();

		clk.notNil.if({
			clock = clk;
		});

		beatfigures = [[[1, \downbeat]],
			[[0.5, \downbeat], [0.5, \offbeat]],
			[[0.334, \downbeat], [0.333, \offbeat], [0.333, \offbeat]],
			[[0.5, \downbeat], [0.25, \offbeat], [0.25, \offbeat]]
		];
		restfigures = [[[1, \rest]],
			[[0.5, \rest], [0.5, \offbeat]],
			[[0.334, \upbeat,], [0.333, \rest], [0.333, \offbeat]]
		];


		fx = DubFx(clock, this);

	}

	beatfigures{|dr|
		var figs;
		dr.notNil.if({
			dr = this.at(dr);
			figs = dr.beatfigures;
		});
		figs.isNil.if({
			figs = beatfigures;
		});
		^figs
	}

	restfigures{|dr|
		var figs;
		dr.notNil.if({
			dr = this.at(dr);
			figs = dr.restfigures;
		});
		figs.isNil.if({
			figs = restfigures;
		});
		^figs
	}


	connect {|name|
		init(nil, name)
	}

	add{|name, instr|
		drums[name] = instr;
	}

	alias {|dr|
		var result;

		result = alias[dr.asSymbol];
		result.isNil.if({ result = dr });

		^result
	}

	at {|dr|
		var result;
		dr.isKindOf(DubInstrument).if({
			result = dr;
		} , {
			result = drums.at(this.alias(dr));
		});
		result.isNil.if({ result = dr });
		^result;
	}

	all {
		^drums.values;
	}

	pdef{|dr=\bd|
		^Pdef(this.alias(dr));
	}

	open {|dr=\cr, quant=1|
		dr = this.at(dr);//drums.at(this.alias(dr));
		//Pdef(dr,
		//	Pbindf(
		//		drums.at(dr),
		//		//\foo, Pfunc({dr.postln}),
		//		\dur, loop,
		//		\db, -9// + Pfunc({|e| ~rave.groove.isStrong(e.beat.value).if({2}, {0})}),
		//	)
		//);
		/*
		Pdef(dr).isPlaying.not.if({
		Pdef(dr).playExt(clock, nil, quant);
		//Pdef(dr).play(clock, nil, quant);
		Pdef.isPlaying = true;
		});
		*/
		//Pdef(dr).isPlaying.not.if({
		//	Pdef(dr).quant_([loop , 0, 0, 1]);
		//});
		dr.loop = [[loop, dr.downbeat]];
		dr.play(clock, quant:[loop , 0, 0, 1]);

		//this.play(dr, quant);
	}



	makeBeat{|probability, beats|
		probability.isNil.if({ probability = [1] });
		beats.isNil.if({ beats = [[[1, \upbeat]]] });
		^beats.wrapAt(probability.normalizeSum.windex)
	}


	rythm{|downprob, downbeats, upprob, upbeats, fillprob, fillbeats|
		var seq, count;

		downprob.isNil.if({
			downprob =[0.6, 0.125, 0.25, 0.125];
		});
		downbeats.isNil.if({
			downbeats=beatfigures;
		});
		upprob.isNil.if({
			upprob=[0.6, 0.25, 0.25];
		});
		upbeats.isNil.if({
			upbeats=restfigures;
		});
		fillprob.isNil.if({
			fillprob = upprob;
		});
		fillbeats.isNil.if({
			fillbeats = upbeats;
		});

		seq = Array(loop);
		count =0;

		((loop/2).ceil.asInt -1).do({
			seq = seq.add(this.makeBeat(downprob, downbeats));
			seq = seq.add(this.makeBeat(upprob, upbeats));
			count = count +2;
		});

		(loop - count > 1).if({
			seq = seq.add(this.makeBeat(downprob, downbeats));
			count = count +1;
		});
		(loop - count > 0).if({
			seq = seq.add(this.makeBeat(fillprob, fillbeats));
			//count = count +1;
			// should do a fill!
		});


		seq = seq.flatten(1);

		^seq
	}



	makeBeats {|dr=\bd, quant=1, downprob, downbeats, upprob, upbeats, fillprob, fillbeats|

		var seq;
		dr = this.at(dr);//drums.at(this.alias(dr));

		/*
		seq = Array(loop);

		(loop/2).do({
		seq = seq.add(this.makeBeat(downprob, downbeats));
		seq = seq.add(this.makeBeat(upprob, upbeats));
		});
		seq = seq.flatten(1);
		*/
		downbeats.isNil.if({
			downbeats = this.beatfigures(dr);
		});
		upbeats.isNil.if({
			upbeats = this.restfigures(dr);
		});

		downbeats.postln;
		upbeats.postln;

		seq = this.rythm(downprob, downbeats, upprob, upbeats, fillprob, fillbeats);
		seq.postln;

		//seqs[dr]=seq;

		//Pdef(dr,
		//	Pbindf(
		//		drums.at(dr),
		//		//\foo, Pfunc({dr.postln}),
		//		[\dur, \db], Pseq(seq, inf)
		//		//\bar, Pfunc({|e| dr.post; " ".post; e[\dur].post; " ".post; e[\db].postln})
		//	)
		//);

		//this.play(dr, quant);
		dr.loop = this.pr_rests(dr, seq);
		dr.play(clock, quant);
	}

	play{|dr, quant = 1|
		/*
		var isp;

		dr = this.alias(dr);
		clock.isKindOf(ExternalClock).if({
		isp = isPlaying[dr];
		isp.isNil.if({ isp = false });
		isp.not.if({
		Pdef(dr).playExt(clock, nil, quant);
		isPlaying[dr] = true;
		});
		} , {
		Pdef(dr).isPlaying.not.if({
		Pdef(dr).play(clock, nil, quant);
		});
		});
		*/
		/*
		Pdef(dr).isPlaying.not.if({
		clock.isKindOf(ExternalClock).if({
		Pdef(dr).playExt(clock, nil, quant);
		//Pdef.isPlaying = true;
		} , {
		Pdef(dr).play(clock, nil, quant);
		});
		});
		*/
		DeprecatedError().throw
	}



	probs {|dr=\bd, quant=1, downprob=0.6, upprob=0.7|
		dr =this.alias(dr);
		downprob.isKindOf(SimpleNumber).if({
			downprob = [downprob, 0.125, 0.25, 0.125];
			downprob.normalizeSum.postln;
		});

		upprob.isKindOf(SimpleNumber).if({
			upprob = [upprob, 0.25, 0.25]
		});

		this.makeBeats(dr, quant,
			downprob, this.beatfigures(dr),
			upprob,this.restfigures(dr)
		);
	}

	down {|dr=\bd, quant=1|
		dr = this.alias(dr);
		this.makeBeats(dr, quant,
			[0.6, 0.125, 0.25, 0.125],
			this.beatfigures(dr),
			[0.6, 0.25, 0.25],
			this.restfigures(dr)
		);
		/*
		var seq;
		seq = Array(loop);

		(loop/2).do({
		seq = seq.add(this.makeBeat(
		[0.6, 0.125, 0.25, 0.125],
		[[[1, downbeat]],
		[[0.5, downbeat], [0.5, offbeat]],
		[[0.334, downbeat], [0.333, offbeat], [0.333, offbeat]],
		[[0.5, downbeat], [0.25, offbeat], [0.25, offbeat]]
		]

		));
		seq = seq.add(this.makeRest(
		[0.6, 0.25, 0.25],
		[[[1, Rest]],
		[[0.5, Rest], [0.5, offbeat]],
		[[0.334, upbeat,], [0.333, Rest], [0.333, offbeat]]
		]

		));
		});
		seq = seq.flatten(1);
		seq.postln;

		seqs[dr]=seq;

		Pdef(dr,
		Pbindf(
		drums.at(dr),
		//\foo, Pfunc({dr.postln}),
		[\dur, \db], Pseq(seq, inf),
		\bar, Pfunc({|e| dr.post; " ".post; e[\dur].post; " ".post; e[\db].postln})
		)
		);
		Pdef(dr).isPlaying.not.if({
		Pdef(dr).playExt(clock, nil, quant);
		});
		*/

	}


	sparsen {|dr=\snare|
		var seq, found, thinner;

		dr = this.at(dr);//drums.at(this.alias(dr));

		seq = dr.loop; //seqs[dr];
		seq.notNil.if({
			found = false;

			thinner = {|match|
				var i, tuple, index;
				i = 0;
				{(i < seq.size) && found.not}.while({
					index = seq.size.rand;
					tuple = seq[index];
					(/*(tuple.last != Rest) || tuple.last.isKindOf(Rest)*/ Dub.pr_isRest(tuple).not).if({
						(tuple.last <= match).if({
							tuple[1] = dr.rest;
							seq[index] = tuple;
							found = true;
						});
					});
					i = i+1;
				});
				found;
			};

			thinner.(dr.offbeat).not.if({
				thinner.(dr.upbeat).not.if({
					thinner.(dr.downbeat).not.if({
						//any beat
						thinner.(100);
			})})});

			found.if ({
				//seqs[dr] = seq;
				//Pdef(dr,
				//	Pbindf(
				//		drums.at(dr),
				//		[\dur, \db], Pseq(seq, inf)
				//		//\bar, Pfunc({|e| dr.post; " ".post; e[\dur].post; " ".post; e[\db].postln})
				//	)
				//);
				dr.modify(/*this.pr_rests(dr, seq)*/ seq);
			});
		});
	}

	floor4 {|dr=\bd, quant=1|
		var seq;

		dr = this.at(dr);//drums.at(this.alias(dr));

		//Pdef(dr,
		//	Pbindf(
		//		drums.at(dr),
		//		//\foo, Pfunc({dr.postln}),
		//		\dur, 2,
		//		\db, downbeat// + Pfunc({|e| ~rave.groove.isStrong(e.beat.value).if({2}, {0})}),
		//	)
		//);
		//Pdef(dr).isPlaying.not.if({
		//	Pdef(dr).quant_([loop , 0, 0, 1]);
		//});
		//this.play(dr, quant);
		/*
		Pdef(dr).playExt(clock, nil, quant);
		//Pdef(dr).play(clock, nil, quant);
		Pdef.isPlaying = true;
		});
		*/
		seq = (loop/2).ceil.asInt.collect({[[1, dr.downbeat], [1, dr.rest]]});
		seq = seq.flatten(1);
		//seqs[dr] = seq;
		dr.loop = seq;//this.pr_rests(dr, seq);
		dr.play(clock, quant);
	}

	up {|dr=\snare, quant=1|
		var seq;

		dr = this.at(dr);//drums.at(this.alias(dr));

		quant.isKindOf(SimpleNumber).if({
			quant = Quant(quant, 1);
		});

		//Pdef(dr,
		//	Ptpar([1,
		//		Pbindf(
		//			drums.at(dr),
		//			//\foo, Pfunc({dr.postln}),
		//			\dur, 2,
		//			\db, upbeat// + Pfunc({|e| ~rave.groove.isStrong(e.beat.value).if({2}, {0})}),
		//	)], 1)
		//);
		//Pdef(dr).isPlaying.not.if({
		//	Pdef(dr).quant_([loop , 1, 0, 1]);
		//});
		//Pdef(dr).playExt(clock, nil, quant);
		////Pdef(dr).play(clock, nil, quant);
		//Pdef.isPlaying = true;
		//});
		//this.play(dr, quant);

		seq = (loop/2).ceil.asInt.collect({[[1, dr.upbeat], [1, dr.rest]]});
		seq = seq.flatten(1);
		//seqs[dr] = seq;
		dr.loop = seq;//this.pr_rests(dr, seq);
		dr.play(clock, [loop, 1, 0, 1]);

	}

	subdivide {|dr=\snare|
		var seq, i, index, found, db, dur, tuple, div, min, newseq;

		dr = this.at(dr);//drums.at(this.alias(dr));

		seq = dr.loop; // seqs[dr];
		seq.notNil.if({
			found = false;
			i = 0;

			{(i < seq.size) && found.not}.while({
				index = seq.size.rand;
				tuple = seq[index];
				(/*(tuple.last != Rest) || tuple.last.isKindOf(Rest).not*/ Dub.pr_isRest(tuple).not).if({

					div = [
						[[0.5, db], [0.5, dr.offbeat]],
						[[0.334, db], [0.333, dr.rest], [0.333, dr.offbeat]],
						[[0.334, db], [0.333, dr.offbeat], [0.333, dr.offbeat]],
						[[0.5, db], [0.25, dr.offbeat], [0.25, dr.offbeat]]
					].wrapAt([0.125, 0.125, 0.25, 0.125].normalizeSum.windex);

					newseq = this.pr_replace(index, tuple, seq, div);
					found = (newseq !=seq);
					seq = newseq;
				});
				i = i+1;
			});


			found.if ({

				//seqs[dr] = seq;
				//Pdef(dr,
				//	Pbindf(
				//		drums.at(dr),
				//		[\dur, \db], Pseq(seq, inf)
				//		//\bar, Pfunc({|e| dr.post; " ".post; e[\dur].post; " ".post; e[\db].postln})
				//	)
				//);
				dr.modify(/*this.pr_rests(dr, seq)*/seq);
			});
		});
	}

	sub {|dr=\clap|
		//dr = this.alias(dr);
		this.subdivide(dr);
	}

	* pr_isRest {|tuple|

		^ ((tuple.last == Rest)|| tuple.last.isKindOf(Rest) || (tuple.last == \rest) || (tuple.last <= 0.ampdb))
	}

	pr_rests { |dr, seq|
		var ret;

		ret = seq.collect({|tuple|
			Dub.pr_isRest(tuple).if({
				//"replacing rest % with %".format(tuple.last, dr.rest).postln;
				[tuple.first, dr.rest]
			} , {
				tuple
			});
		});
		^ret;
	}

	thicken {|dr=\clap|
		var seq, i, index, found, db, dur, tuple, div, min, newseq;

		dr = this.at(dr);//drums.at(this.alias(dr));



		seq = dr.loop; //seqs[dr];
		seq.notNil.if({
			found = false;
			i = 0;



			{(i < seq.size) && found.not}.while({
				index = seq.size.rand;
				tuple = seq[index];
				(Dub.pr_isRest(tuple)).if({

					div = (this.restfigures(dr)).copy;
					div.removeAt(0);
					div = div.wrapAt([0.25, 0.25].normalizeSum.windex);
					/*
					div = div.collect({|item|
					item[0] = item.first * dur;
					min = item.first.min(min);
					item;
					});

					(min >= 0.08).if ({ // let's not get too small
					seq.removeAt(index);
					div.do({|item, ind| seq = seq.insert(ind+index, item)});
					//seqs[dr]=seq;
					found = true;
					}, { "too small".postln; });*/
					newseq = this.pr_replace(index, tuple, seq, div);
					found = (newseq !=seq);
					seq = newseq;


				}, {
					db = tuple.last;
					//dur = tuple.first;
					//min = dur;

					div = [
						[[0.5, db], [0.5, dr.offbeat]],
						[[0.334, db], [0.333, dr.rest], [0.333, dr.offbeat]],
						[[0.334, db], [0.333, dr.offbeat], [0.333, dr.offbeat]],
						[[0.5, db], [0.25, dr.offbeat], [0.25, dr.offbeat]]
					].wrapAt([0.125, 0.125, 0.25, 0.125].normalizeSum.windex);


					newseq = this.pr_replace(index, tuple, seq, div);
					found = (newseq !=seq); // newseq and seq are same -> not found, different-> found
					seq = newseq;

				});

				i = i+1;
			});
			found.if ({
				//seqs[dr] = seq;
				//Pdef(dr,
				//	Pbindf(
				//		drums.at(dr),
				//		[\dur, \db], Pseq(seq, inf)
				//		//\bar, Pfunc({|e| dr.post; " ".post; e[\dur].post; " ".post; e[\db].postln})
				//	)
				//);
				dr.modify(/*this.pr_rests(dr, seq)*/seq);
			});
		});

	}


	pr_replace {|index, tuple, seq, div|

		var min, dur, db;

		dur = tuple.first;

		db = tuple.last;
		(Dub.pr_isRest(tuple) /*db.isKindOf(Rest) || db == Rest*/).if({
			db = \offbeat;
		});

		div.postln;
		div.first[1] = db;

		min = dur;

		div = div.collect({|item|
			item[0] = item.first * dur;
			min = item.first.min(min);
			item;
		});

		(min >= 0.08).if ({ // let's not get too small
			seq.removeAt(index);
			div.do({|item, ind| seq = seq.insert(ind+index, item)});
			//seqs[dr]=seq;
			//found = true;
		}, { "too small".postln; });

		^seq;
	}


	clock_ {|newclock|
		clock = newclock;
		fx.clock = clock;
	}

	stop {|...args|
		var crash, bd, clk;
		crash = this.at(\crash);
		bd = this.at(\kick);

		fx.stop(*args);

		stopf.notNil.if({
			stopf.value(*args);
		});

		clk = clock;
		clk.isKindOf(ExternalClock).if({
			clk = clock.tempoclock;
		});


		crash.notNil.if({
			Pbindf(
				crash.pbind,
				\dur, Pseq([1], loop),
				\db, crash.downbeat
			).play(clk, quant:[4,1]);
		});
		bd.notNil.if({
			Pbindf(
				bd.pbind,
				\dur, Pseq([1], loop),
				\db, bd.downbeat
			).play(clk, quant:[4,1]);
		});

		clk.playNextBar({
			drums.do({|d|
				d.stop
			});

		});
	}
}



DubFx {

	var <>clock, dub, >s, routing, <groups, busses, <bufs, <odds, <>stopf;

	*new {|clock, dub, server|
		^super.new.init(clock, dub, server)
	}

	init {|clk, db, server|
		clock = clk;
		dub = db;
		routing = IdentityDictionary();
		groups = IdentityDictionary();
		busses = IdentityDictionary();
		bufs = IdentityDictionary();
		odds = IdentityDictionary();
		s = server;
	}

	*initClass{

		StartUp.add {
			SynthDef(\dubfx, {|out = 0, amp=0.2, in=0, gate = 1, tempo=1|

				var trig, note, son, sweep, mul, env, input, follower;


				input = SoundIn.ar(in);
				follower = Amplitude.kr(input);

				trig = CoinGate.kr(0.5, Impulse.kr(/*2*/ tempo.reciprocal));
				sweep = LFSaw.ar(
					Demand.kr(trig, 0,
						Drand(
							Control.names([\fxsweep]).ir([1, 2, 2, 3, 4, 5, 6, 8, 16]) * tempo.reciprocal,
							//[1, 2, 2, 3, 4, 5, 6, 8, 16],
							inf)
				)).exprange(40, 5000);

				son = input; // Pulse.ar(note * [0.99, 1, 1.01]).sum;
				son = LPF.ar(son, sweep);
				son = Normalizer.ar(son);
				son = son + BPF.ar(son, 2000, 2);

				//////// special flavours:
				// hi manster
				son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, HPF.ar(son, 1000) * 4]);
				// sweep manster
				son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, HPF.ar(son, sweep) * 4]);
				// decimate
				son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, son.round(0.1)]);

				son = (son * 5).tanh;
				//son = son + GVerb.ar(son, 10, 0.1, 0.7, mul: 0.3);
				//son.dup;

				env = EnvGen.kr(Env.asr, gate, doneAction:2);

				Out.ar(out, son.dup * amp * env * follower);
			}).writeDefFile;


			SynthDef(\wub, {|out = 0, amp=0.2, tempo=1, gate = 0, freq=440|

				var trig, son, sweep, mul, env, loudness;

				loudness = LagUD.kr(amp, 0.01, tempo/3);
				env = EnvGen.kr(Env.asr, gate, doneAction:2) * loudness;


				trig = CoinGate.kr(0.5, Impulse.kr(/*2*/ tempo.reciprocal));

				sweep = LFSaw.ar(
					Demand.kr(trig, 0,
						Drand(
							Control.names([\sweep]).ir([1, 2, 2, 3, 4, 5, 6, 8, 16]) * tempo.reciprocal,
							inf)
				)).exprange(40, 5000);

				son = Pulse.ar(freq * [Rand(0.97, 0.99), 1, Rand(1.01, 1.03)]).sum;
				son = LPF.ar(son, sweep);
				son = Normalizer.ar(son);
				son = son + BPF.ar(son, 2000, 2);

				//////// special flavours:
				// hi manster
				son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, HPF.ar(son, 1000) * 4]);
				// sweep manster
				son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, HPF.ar(son, sweep) * 4]);
				// decimate
				son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, son.round(0.1)]);
				//gverb
				son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, son + GVerb.ar(son, 10, 0.1, 0.7, mul: 0.3)]);

				son = (son * 5).tanh / 4;


				Out.ar(out, son * env);

			}).writeDefFile;


			2.do({|i|

				SynthDef(\loopfx++(i+1), {|out = 0, amp=0.2, tempo=1, gate = 0, freq=440, bufnum|

					var trig, son, sweep, mul, env, loudness;

					loudness = LagUD.kr(amp, 0.01, tempo/3);
					env = EnvGen.kr(Env.asr, gate, doneAction:2) * loudness;


					trig = CoinGate.kr(0.5, Impulse.kr(/*2*/ tempo.reciprocal));

					sweep = LFSaw.ar(
						Demand.kr(trig, 0,
							Drand(
								Control.names([\sweep]).ir([1, 2, 2, 3, 4, 5, 6, 8, 16]) * tempo.reciprocal,
								inf)
					)).exprange(40, 5000);

					son = PlayBuf.ar(i+1, bufnum, BufRateScale.kr(bufnum), loop:1);

					son = Select.ar(TRand.kr(trig: trig) < 0.05, [son,
						PitchShift.ar(son, pitchRatio:
							[Rand(0.97, 0.99), Rand(1.01, 1.03)],
							pitchDispersion:[Rand(0, 0.01), Rand(0, 0.01)]
						).sum + son
					]);

					son = LPF.ar(son, sweep);
					son = Normalizer.ar(son);
					son = son + BPF.ar(son, 2000, 2);

					//////// special flavours:
					// hi manster
					son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, HPF.ar(son, 1000) * 4]);
					// sweep manster
					son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, HPF.ar(son, sweep) * 4]);
					// decimate
					son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, son.round(0.1)]);
					//gverb
					son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, son + GVerb.ar(son, 10, 0.1, 0.7, mul: 0.3)]);

					son = (son * 5).tanh / 4;


					Out.ar(out, son * env);

				}).writeDefFile;

			});


			SynthDef(\autopanner, {|in = 0, out = 0, window = 200, gate=1|

				var right, left, right_amp, left_amp, diff, durs, shapes;
				var lupper_bound, lubenv, llower_bound, llbenv, lsin;
				var rupper_bound, rubenv, rlower_bound, rlbenv, rsin;
				var trig, dwhite, lspeed, rspeed;
				var left_out, right_out;
				var output, env;

				left = In.ar(in);
				right = In.ar(in + 1);

				//left = SinOsc.ar(400, 0, MouseX.kr(0, 1));
				//right = SinOsc.ar(500, 0.2, MouseY.kr(0, 1));


				left_amp = (RunningSum.kr(left.squared, window) / window).sqrt;
				right_amp = (RunningSum.kr(right.squared, window) / window).sqrt;


				diff = left_amp.ampdb - right_amp.ampdb; // let's do dbs

				trig = Trig.kr(diff > -10, 0.1) +
				Trig.kr(diff < -10, 0.1) +
				Trig.kr(diff > -6, 0.1) +
				Trig.kr(diff < -6, 0.1) +
				Trig.kr(diff > -1.5, 0.1) +
				Trig.kr(diff < -1.5, 0.1) +
				Trig.kr(diff > 1.5, 0.1) +
				Trig.kr(diff < 1.5, 0.1) +
				Trig.kr(diff > 6, 0.1) +
				Trig.kr(diff < 6, 0.1) +
				Trig.kr(diff > 10, 0.1) +
				Trig.kr(diff < 10, 0.1);

				dwhite = Dwhite(0.005, 0.05,inf);
				lspeed = Demand.kr(trig, 0, dwhite);
				rspeed = Demand.kr(trig, 0, dwhite);

				// to change the speed based on /where/ the trigger has happened, you can either do
				// another envelope to adjust the upper and lower bounds OR
				// you can make an array of Dqhites and then multiply the Trigs by integers and use
				// them as an index to an array.

				//((left_amp - right_amp) + 1) /2; // scale to between 0-1

				// if they're equal, pan them between 1, -1 and 0.7 and -0.7

				// if one is really much bigger than the other, pan it to 0
				// and the other one can slowly sway between 0.5 and -0.5

				//durs = [0.2, 0.25, 0.1, 0.25, 0.2];
				durs = [6, 1, 6];
				shapes = [\sin, \lin, \sin];
				lubenv = Env([0.5, -0.8, -0.8, 0], durs, shapes);
				llbenv = Env([-0.5, -1, -1, 0],  durs, shapes);
				rubenv = Env([0, 1, 1, 0.5],  durs, shapes);
				rlbenv = Env([0, 0.8, 0.8, -0.5],  durs, shapes);

				lupper_bound = IEnvGen.kr(lubenv, diff+ 6.5);
				llower_bound = IEnvGen.kr(llbenv, diff + 6.5);

				lsin = (((SinOsc.kr(lspeed,
					Rand(0.0, 1.0),
					0.5, 0.5)) * (lupper_bound - llower_bound))
				+ llower_bound);


				rupper_bound = IEnvGen.kr(rubenv, diff+ 6.5);
				rlower_bound = IEnvGen.kr(rlbenv, diff+ 6.5);

				rsin = (((SinOsc.kr(rspeed,
					Rand(0.0, 1.0),
					0.5, 0.5)) * (rupper_bound - rlower_bound))
				+ rlower_bound);

				//lsin = IEnvGen.kr(InterplEnv([0, -1, 0], [-9, 0] + 9, [\sin, \sin]), diff+ 9);
				//rsin = IEnvGen.kr(InterplEnv([0, 1, 0], [-9, 0] + 9, [\sin, \sin]), diff+ 9);


				left_out = Pan2.ar(left, Lag.kr(lsin, 0.08));
				right_out = Pan2.ar(right, Lag.kr(rsin, 0.08));

				output = (left_out + right_out).tanh;
				env=EnvGen.kr(Env.asr, gate, doneAction:2);

				Out.ar(out, output * env);
				//Out.ar(out, right_out.tanh);
				//Out.kr(b, diff);
				//Out.kr(c, lsin);
				//Out.kr(d, rsin);
				//Out.ar(bus.index, output);
			}).writeDefFile;

			SynthDef(\GMP98, {|freq=440, amp=0.2, gate=1, out=0, pan=0|
				var saw1, saw2, env, pitch, pitchenv, panner;

				env = EnvGen.kr(Env.asr(1, 1, 2), gate, doneAction:2) * amp/2;
				pitch = freq.cpsmidi + 7;
				pitchenv = XLine.kr(pitch - Rand(0.01, 0.25), pitch, Rand(0.4, 0.5));
				saw1 = Saw.ar(freq);
				saw2 = Saw.ar(pitchenv.midicps);
				panner = Pan2.ar(saw1 + saw2, pan, env);
				Out.ar(out, panner);
			}).writeDefFile;

			SynthDef(\rs, {|in = 0, out = 0, gate=1| // route stereo
				var input, env;
				input = SoundIn.ar([in, in+1]);
				env=EnvGen.kr(Env.asr, gate, doneAction:2);
				Out.ar(out, input.tanh * env);
			}).writeDefFile;

			SynthDef(\rm, {|in = 0, out = 0, gate=1| // route mono
				var input, env;
				input = SoundIn.ar(in);
				env=EnvGen.kr(Env.asr(releaseTime:5), gate, doneAction:2);
				Out.ar(out, input.tanh * env);
			}).writeDefFile;

			SynthDef(\ts, {|in = 0, out = 0, gate=1| // route mono
				var input, env;
				input = In.ar([in, in+1]);
				env=EnvGen.kr(Env.asr, gate, doneAction:2);
				ReplaceOut.ar(out, input.tanh * env);
			}).writeDefFile;
			/*
			SynthDef("plucking", {arg amp = 0.1, freq = 440, decay = 5, coef = 0.1, pan=0;
			var env, snd, panner, verb, lpf, mask;
			env = EnvGen.kr(Env.linen(0, decay, 0), doneAction: 2);
			snd = Pluck.ar(
			in: WhiteNoise.ar(amp),
			trig: Impulse.kr(0),

			maxdelaytime: 0.1,
			delaytime: freq.reciprocal,
			decaytime: decay,
			coef: coef);

			//verb = FreeVerb.ar(snd);
			mask = MantissaMask.ar(snd, 7);
			lpf = LPF.ar(mask, freq*1.5);
			panner = Pan2.ar(lpf, pan);
			Out.ar(0, panner);
			}).writeDefFile;
			*/
			SynthDef("plucking", {arg out=0, amp = 0.1, freq = 440, decay = 5, coef = 0.1, pan=0;
				var env, snd, panner, verb, lpf, mask;
				env = EnvGen.kr(Env.linen(0, decay, 0), doneAction: 2);
				snd = Pluck.ar(
					in: PinkNoise.ar(amp),//WhiteNoise.ar(amp),
					trig: Impulse.kr(0),

					maxdelaytime: 0.1,
					delaytime: (freq/2).reciprocal,
					decaytime: decay,
					coef: coef);

				//verb = FreeVerb.ar(snd);
				mask = MantissaMask.ar(snd, 7);
				lpf = BPF.ar(mask, freq);
				panner = Pan2.ar(lpf* 20, pan, amp);
				Out.ar(out, panner);
			}).writeDefFile;


				SynthDef(\record1, {|in=2, gate=1, bufnum=2|
					var env, input, rec;

					input = AudioIn.ar(in);
					env = EnvGen.kr(Env.asr(0.01, releaseTime:0.01), gate, doneAction:2);
					input = input * env;
					rec = RecordBuf.ar(input,bufnum, preLevel:1);
				}).writeDefFile;

			SynthDef(\record2, {|in=2, gate=1, bufnum=2|
					var env, input, rec;

				input = [AudioIn.ar(in), AudioIn.ar(in+1)];
					env = EnvGen.kr(Env.asr(0.01, releaseTime:0.01), gate, doneAction:2);
					input = input * env;
					rec = RecordBuf.ar(input,bufnum, preLevel:1);
				}).writeDefFile;


		}

	}

	s {
		var ret;
		ret = s;
		s.isNil.if({ ret = Server.default });
		^ret
	}

	makeBuffer {|numChannels=1, key|
		var frames, srv, buf, clk;

		srv = this.s;
		clk = clock;
		clk.isKindOf(ExternalClock).if({ clk = clk.tempoclock});

		frames = dub.loop * clock.tempo * srv.sampleRate;

		buf = Buffer.alloc(srv, frames, numChannels);

		key.notNil.if({
			bufs[key.asSymbol] = buf;
		});

		^buf

	}


	play{|key, name, args, group|
		var syn, grp;

		group.isNil.if({
			grp = Group(this.s);
			groups[key] = grp;
		} , {
			group.isKindOf(Group).if({
				grp = group;
				groups[key] = grp;
			}, {
				grp = groups.at(group.asSymbol);
				grp.isNil.if({
					grp = Group(this.s);
					groups[group.asSymbol] = grp;
				})
			})
		});

		syn = (instrument: name, dur: inf, group:grp).proto_(args);
		//syn.proto_(args);

		clock.isKindOf(ExternalClock).if({

			syn = syn.play(clock.tempoclock);
		} , {
			syn = syn.play(clock);
		});

		this.add(key, syn);
		^syn;
	}


	add{|key, synth|
		routing[key.asSymbol] = synth;
	}

	at {|key|
		^routing[key.asSymbol]
	}

	remove {|key|
		var syn, node;
		//"gate to 0 for %".format(key).postln;
		syn = routing[key.asSymbol];
		syn.notNil.if ({
			syn.set(\gate, 0);
			"gate to 0 for %".format(key).postln;
			routing.removeAt(key.asSymbol);
			node = Node.basicNew(s, syn.nodeID);
			node.set(\gate, 0)
		});

		^syn
	}

	g { ^ groups }

	bus {|key, chan=2|
		var ret;

		ret = busses[key];
		ret.isNil.if({
			ret = Bus.audio(this.s, chan);
			busses[key] = ret;
		});
		^ret
	}

	stop {|...args|

		var clk;

		stopf.notNil.if({
			stopf.value(*args);
		});

		clk = clock;
		clk.isKindOf(ExternalClock).if({
			clk = clock.tempoclock;
		});

		clk.playNextBar({

			routing.do({|r|
				r.release(0.1);
			});
		});

	}

}