{
	int i, j, k, w;
	i = random;
	j = random;
	k = random;
	w = i;
	while ( k < j + i) {
		while (w < k) {
			assert w < j + i;
			assert k > j;//Warn
			w = w + 1;
		}
		assert w < j + i + 10;//Warn, false alarm
		assert w > j + 1;//Warn
		k = k + 1;
	}
	assert k >= j + i;
	assert w > j + 1;//Warn
}
