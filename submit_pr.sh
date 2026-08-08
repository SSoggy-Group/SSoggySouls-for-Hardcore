git commit -am "refactor(common): optimize visible deaths filtering"
gh pr create --title "⚡ Bolt: [Optimize visible deaths filtering]" --body "💡 What: Hoisted invariant \`Instant\` time threshold allocations outside of the loop in \`DlcDeaths.visibleDeaths\` and reordered logic to short-circuit when a death meets the public threshold, bypassing the expensive \`DlcSocial.getRelationTo()\` lookup.

🎯 Why: \`DlcSocial.getRelationTo()\` performs a synchronized lookup against \`DlcStorage\` which accesses a \`Properties\` map. Calling this inside a loop for every death record caused unnecessary overhead, particularly for older deaths that were already globally visible. Furthermore, redundant \`now.minusSeconds(...)\` object allocations inside the loop added GC pressure.

📊 Impact: Reduces object allocations during death visibility checks and significantly decreases synchronous storage lookups for servers with large obituaries.

🔬 Measurement: Check the execution time and GC allocation rate of \`DlcDeaths.visibleDeaths\` during frequent \`/obituaries\` command usage or death events, observing reduced memory pressure and faster loop execution times."
