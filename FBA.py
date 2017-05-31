from pulp import *
import math

#Declaring LP variables as reaction rates
tn = 170

atpmaint = LpVariable("atpmaint", 15) #maintenance cost of 15 umol/gDW*min
atpgrow = LpVariable("atpgrow", 0)
fba = LpVariable("fba", 0)
gpm = LpVariable("gpm", 0)
hxk = LpVariable("hxk", 0, 15) #hexosekinase 
pdc = LpVariable("pdc", 0) #pyruvate decarboxylase
pfk = LpVariable("pfk", 0, 15) #phosphofructosekinase
pgi = LpVariable("pgi", 0)
pgk = LpVariable("pgk", 0)
pyk = LpVariable("pyk", 0) #pyruvate kinase
gapdh = LpVariable("gapdh", 0)
tpi = LpVariable("tpi", 0)
ugp = LpVariable("ugp", 0)
hxt = LpVariable("hxt", 15) #maximal glucose uptake in umol / min·g
gnd = LpVariable("gnd", 0) #6-phosphogluconatedehydrogenase
rki = LpVariable("rki", 0)
rpe = LpVariable("rpe", 0)
sol = LpVariable("sol", 0) #gluconolactonase
tal = LpVariable("tal", 0)
tklef = LpVariable("tklef")
tklrs = LpVariable("tklrs")
zwf = LpVariable("zwf", 0, 15) #glucose-6-phosphatedehydrogenase
eno = LpVariable("eno", 0)
nadphox = LpVariable("nadphox", 0)
tca = LpVariable("tca", 0)
etc1 = LpVariable("ect1", 0)
etc2 = LpVariable("ect2", 0)
os = LpVariable("os", 22)
collection = LpVariable("collection", 25)
atpprot = LpVariable("atpprot", 4*0.00131*22/(tn*60)) #energy cost for maintaining 25 umol/gDW*min specific activity
#atpprot = LpVariable("atpprot", 0, 0)

#Defining LP problem
prob = LpProblem("growth", LpMaximize)

#Setting up steady state constraints
prob += hxt - hxk == 0 #GLC: glucose
prob += - zwf - pgi + hxk == 0 #G6P: glucose-6-phosphate
prob += tal + tklef + pgi - pfk == 0 #F6P: fructose-6-phosphate
prob += pfk - fba == 0 #F16bP: fructose-1,6-bisphosphate
prob += fba - tpi == 0 #DHAP
prob += tklef - tal + tklrs + tpi - gapdh + fba == 0 #GAP: glyceraldehyde-3-phosphate
prob += gapdh - pgk == 0 #BPG: bisphosphogluconate
prob += - gpm + pgk == 0 #P3G: phospho-3-glycerate
prob += gpm - eno == 0 #P2G: phospho-2-glycerate
prob += - pyk + eno == 0 #phosphoenolpyruvate
prob += pyk - pdc == 0 #PYR: pyruvate
prob += pdc - tca == 0 #acetaldehyde
prob += tca - etc2 == 0 #FADH2
prob += - tca + etc2 == 0 #FAD
prob += - gapdh - 3*tca + etc1 - pdc == 0 #NAD
prob += 3*tca + gapdh - etc1 + pdc == 0 #NADH
#prob += - hxk - pfk + pgk + pyk + tca + 2.5*etc1 + 1.5*etc2 - atpmaint - atpgrow == 0 #ATP
#prob += hxk + pfk - pgk - pyk - tca - 2.5*etc1 - 1.5*etc2 + atpmaint + atpgrow == 0 #ADP
prob += - hxk - pfk + pgk + pyk + tca + 2.5*etc1 + 1.5*etc2 - atpmaint - atpgrow - atpprot == 0 #ATP
prob += hxk + pfk - pgk - pyk - tca - 2.5*etc1 - 1.5*etc2 + atpmaint + atpgrow + atpprot == 0 #ADP
prob += - sol + zwf == 0 #G6L: glucose-6-phosphatelactone (6-phosphogluconolactone)
prob += sol - gnd == 0 #P6G: 6-phosphogluconate
prob += gnd + zwf - nadphox - os == 0 #NADPH
prob += nadphox - zwf - gnd + os == 0 #NADP
prob += gnd - rpe - rki == 0 #Ru5P: ribulose-5-phosphate
prob += rpe - tklef - tklrs == 0 #X5P: xylulose-5-phosphate
prob += - tklrs + rki == 0 #R5P: ribose-5-phosphate
prob += tal - tklef == 0 #E4P: erythrose-4-phosphate
prob += -tal + tklrs == 0 #S7P: sedoheptulose-7-phosphate
prob += 0.39999999999999997*(atpgrow + atpmaint + pfk + hxk + atpprot) - 30.933333244382023*(nadphox) <= 0
#prob += 0.25*pgi - zwf == 0 #balance division of glucose betweeen glycolysis and PPP

#Defining the objective function
#prob += collection
prob += atpgrow #maximize growth

LpSolverDefault.msg = 1

prob.writeLP("Metabolism and Growth")

prob.solve()

print("Status: ", LpStatus[prob.status])
print("atpase velocity: " + str(value(atpgrow + atpmaint)) + " umol/gDW*min"+"\n", \
	"NADPH oxidation velocity: " + str(value(nadphox + os)) + " umol/gDW*min" + "\n", \
	"Growth Percent per minute: " + str(value(28.8 * (atpgrow + atpmaint + hxk + pfk) / 1e6 * 100)) + "\n", \
	"Growth Percent per 90 minutes: " + str(value(28.8 * (atpgrow + atpmaint + hxk + pfk) / 1e6 * 100 * 90)) + "\n", \
	"Glycolysis rate: " + str(value(pgi)) + " umol/gDW*min" + "\n", \
	"Pentose Phosphate Pathway rate: " + str(value(zwf)) + " umol/gDW*min" + "\n",
	"ATP/glucose: " + str(value(pgk + pyk + tca + 2.5*etc1 + 1.5*etc2 - hxk - pfk)/value(hxt)) + "\n", 
	"NADPH/glucose: " + str(value(gnd + zwf)/value(hxt)) + "\n", 
	"Production rate: " + str(value(collection)) + " umol/gDW*min" + "\n", 
	"CO2 production rate: " + str(value(2*tca + pdc + gnd))+ " umol/gDW*min" + "\n",
	"atpprot: " + str(value(atpprot)) + " umol/gDW*min"
	#"Growth Percent per minute: " + str(value(0.11 * 180.1559 / 30.9333333 * (atpgrow + atpmaint + hxk + pfk)/10e6 * 100)) + "\n", \
	#"Growth Percent per 90 minutes: " + str(value(0.11 * 180.1559 / 30.9333333 * (atpgrow + atpmaint + hxk + pfk)/10e6 * 100 * 90)) + "\n", \
	#"Growth Percent per minute: " + str(value(0.11 * 180.1559 * hxt / 10e6 * 100)) + "\n", \
	#"Growth Percent per 90 minutes: " + str(value(0.11 * 180.1559 * hxt / 10e6  * 100 * 90)) + "\n", \
	)
