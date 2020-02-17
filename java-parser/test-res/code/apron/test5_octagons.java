{
	int i, j, k;
	i = 1;
	j = random;
	k = j;
	k = k + 1;
	assert k >= j +1;
	assert k > j + 1;//Warn
	while(k >= j) {
		k = k + i;
		assert k >= j;
		assert i > j;//Warn
	}
	assert 1==0;//No warn because it's unreachable
}
