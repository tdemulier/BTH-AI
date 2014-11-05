:- lib(ic).

test(N):-
	write(output, N),
	flush(output).
	
test2(1,2).

domains_square(s(Visited,Breeze,Pit,PosX,PosY)):-
	Visited #:: 0..1,
	Breeze #:: 0..1,
	Pit #:: 0..2,
	PosX #:: 1..4,
	PosY #:: 1..4.
	
board([s(V1,B1,P1,1,1),
	s(V2,B2,P2,1,2),
	s(V3,B3,P3,1,3),
	s(V4,B4,P4,1,4),
	s(V5,B5,P5,2,1),
	s(V6,B6,P6,2,2),
	s(V7,B7,P7,2,3),
	s(V8,B8,P8,2,4),
	s(V9,B9,P9,3,1),
	s(V10,B10,P10,3,2),
	s(V11,B11,P11,3,3),
	s(V12,B12,P12,3,4),
	s(V13,B13,P13,4,1),
	s(V14,B14,P14,4,2),
	s(V15,B15,P15,4,3),
	s(V16,B16,P16,4,4)]):-
		domains_square(s(V1,B1,P1,1,1)),
		domains_square(s(V2,B2,P2,1,2)),
		domains_square(s(V3,B3,P3,1,3)),
		domains_square(s(V4,B4,P4,1,4)),
		domains_square(s(V5,B5,P5,2,1)),
		domains_square(s(V6,B6,P6,2,2)),
		domains_square(s(V7,B7,P7,2,3)),
		domains_square(s(V8,B8,P8,2,4)),
		domains_square(s(V9,B9,P9,3,1)),
		domains_square(s(V10,B10,P10,3,2)),
		domains_square(s(V11,B11,P11,3,3)),
		domains_square(s(V12,B12,P12,3,4)),
		domains_square(s(V13,B13,P13,4,1)),
		domains_square(s(V14,B14,P14,4,2)),
		domains_square(s(V15,B15,P15,4,3)),
		domains_square(s(V16,B16,P16,4,4)).
	
getVarList(Board,List):-
	(foreach(s(V,B,P,_,_),Board),
		fromto([],In,Out,List)
		do
		Out = [V,B,P | In]
		
	).
	
pitConst(Board):-
	(foreach(s(_,_,P1,X1,Y1),Board),param(Board) do
		(foreach(s(_,B2,_,X2,Y2),Board),param(P1,X1, Y1) do
			(
				(((abs(X1 - X2) #= 1 and	Y1 #= Y2) or (X1 #= X2 and	abs(Y1 - Y2) #= 1))
				and(P1 #= 1)) => (B2 #= 1)
			)
		)
	).

run(B):-
	board(B),
	getVarList(B,L),
	pitConst(B),
	labeling(L).