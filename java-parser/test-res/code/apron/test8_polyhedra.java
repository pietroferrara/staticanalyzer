{
	int i, j, k, w;
	i = random;
	j = random;
	w = i;
	k = w + 10;
	assert k > i + 9;
	assert k > i + 11;//Warn
	while(w < j) {
		if(j < i)
			assert w < i;
		else j = j +1;
		assert w < j;//Warn, false alarm
		assert j < i;//Warn
		w = w +100;
	}
	assert w >= j - 1;
	assert i >= w;//Warn
}
